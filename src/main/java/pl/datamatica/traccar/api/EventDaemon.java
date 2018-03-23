/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.datamatica.traccar.api;

import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.Maintenance;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.DeviceEventType;
import pl.datamatica.traccar.model.DeviceEvent;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.Device;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.datamatica.traccar.api.utils.GeoFenceCalculator;
import pl.datamatica.traccar.api.utils.GeoUtils;
import static pl.datamatica.traccar.model.DeviceEventType.*;
import pl.datamatica.traccar.model.LastDeviceEventTime;
import pl.datamatica.traccar.model.Route;
import pl.datamatica.traccar.model.RoutePoint;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserDeviceStatus;

public class EventDaemon {
    
    private static EventDaemon instance;
    
    public static EventDaemon getInstance() {
        if(instance == null)
            instance = new EventDaemon();
        return instance;
    }
    
    static class DeviceState {
        Long latestPositionId;
    }

    public static class OfflineDetector implements Runnable {
        /**
         * Id of device <-> Id of position, which has posted offline event
         */
        Map<Long, Long> latestOfflineEvents = new HashMap<>();

        public void run() {
            EntityManager entityManager = Context.getInstance().createEntityManager();
            try {
                entityManager.getTransaction().begin();

                Date currentTime = new Date();

                for (Device device : entityManager.createQuery("SELECT d FROM Device d INNER JOIN FETCH d.latestPosition", Device.class).getResultList()) {
                    Position position = device.getLatestPosition();
                    // check that device is offline
                    long timeout = (long) device.getTimeout() * 1000;
                    if (currentTime.getTime() - position.getTime().getTime() >= timeout
                            && (position.getServerTime() == null
                                || currentTime.getTime() - position.getServerTime().getTime() >= timeout)) {
                        Long latestOfflinePositionId = latestOfflineEvents.get(device.getId());
                        if (latestOfflinePositionId == null) {
                            List<DeviceEvent> offlineEvents = entityManager.createQuery("SELECT e FROM DeviceEvent e WHERE e.position=:position AND e.type=:offline", DeviceEvent.class)
                                    .setParameter("position", position)
                                    .setParameter("offline", DeviceEventType.OFFLINE)
                                    .getResultList();
                            if (!offlineEvents.isEmpty()) {
                                latestOfflineEvents.put(device.getId(), position.getId());
                                latestOfflinePositionId = position.getId();
                            }
                        }

                        if (latestOfflinePositionId == null || latestOfflinePositionId.longValue() != position.getId()) {
                            DeviceEvent offlineEvent = new DeviceEvent();
                            offlineEvent.setTime(currentTime);
                            offlineEvent.setDevice(device);
                            offlineEvent.setType(DeviceEventType.OFFLINE);
                            offlineEvent.setPosition(device.getLatestPosition());
                            entityManager.persist(offlineEvent);
                            latestOfflineEvents.put(device.getId(), position.getId());
                        }
                    }
                }
                entityManager.getTransaction().commit();
            } finally {
                entityManager.close();
            }
        }
    }

    static abstract class EventProducer {
        EntityManager entityManager;

        private Date currentDate;

        void setCurrentDate(Date currentDate) {
            this.currentDate = currentDate;
        }

        void before(EntityManager entityManager) {
            this.entityManager = entityManager;
            before();
        }
        
        abstract void before();

        abstract void positionScanned(Position prevPosition, Position position);

        abstract void after();

        EntityManager entityManager() {
            return entityManager;
        }

        Date currentDate() {
            return currentDate;
        }
    }

    static class PositionProvider {
        EntityManager entityManager;

        /**
         * Scanning is based on assumption that position identifiers are incremented sequentially
         */
        Long lastScannedPositionId;

        public List<Position> getPositions() {
            // find latest position id for the first scan
            if (lastScannedPositionId == null) {
                List<Long> latestPositionId = entityManager.createQuery("SELECT MAX(d.latestPosition.id) FROM Device d WHERE d.latestPosition IS NOT NULL", Long.class).getResultList();
                if (latestPositionId.isEmpty() || latestPositionId.get(0) == null) {
                    return Collections.emptyList();
                } else {
                    lastScannedPositionId = latestPositionId.get(0);
                }
            }

            // load all positions since latest
            List<Position> positions = entityManager.createQuery(
                    "SELECT p FROM Position p INNER JOIN p.device d WHERE p.id > :from ORDER BY d.id, p.time ASC", Position.class)
                    .setParameter("from", lastScannedPositionId)
                    .getResultList();
            return positions;
        }

        public Long getLastScannedPositionId() {
            return lastScannedPositionId;
        }

        public void setLastScannedPositionId(Long lastScannedPositionId) {
            this.lastScannedPositionId = lastScannedPositionId;
        }
        
        public void setEntityManager(EntityManager em) {
            this.entityManager = em;
        }
    }

    static class PositionScanner implements Runnable {
        PositionProvider positionProvider = new PositionProvider();

        Map<Long, DeviceState> deviceState = new HashMap<>();

        List<EventProducer> eventProducers = new ArrayList<>();
        
        public void run() {
            EntityManager entityManager = Context.getInstance().createEntityManager();
            try {
                entityManager.getTransaction().begin();
                positionProvider.setEntityManager(entityManager);

                run(entityManager);

                entityManager.getTransaction().commit();
            } finally {
                entityManager.close();
            }
        }
        
        void run(EntityManager entityManager) {
             // Load positions
            List<Position> positions = positionProvider.getPositions();

            // init event producers
            Date currentDate = new Date();
            for (EventProducer eventProducer : eventProducers) {
                eventProducer.setCurrentDate(currentDate);
                eventProducer.before(entityManager);
            }

            Position prevPosition = null;
            Device device = null;
            DeviceState state = null;
            for (Position position : positions) {
                // find current device and it's state
                if (device == null || device.getId() != position.getDevice().getId()) {
                    device = position.getDevice();
                    state = deviceState.get(device.getId());
                    if (state == null || state.latestPositionId == null) {
                        state = new DeviceState();
                        deviceState.put(device.getId(), state);
                        prevPosition = null;
                    } else {
                        prevPosition = entityManager.find(Position.class, state.latestPositionId);
                    }
                }

                // calculate
                for (int i = 0; i < eventProducers.size(); i++) {
                    eventProducers.get(i).positionScanned(prevPosition, position);
                }

                // update prev position and state
                state.latestPositionId = position.getId();
                prevPosition = position;
                // update latest position id
                positionProvider.setLastScannedPositionId(Math.max(positionProvider.getLastScannedPositionId(), position.getId()));
            }

            // destroy event producers
            for (EventProducer eventProducer : eventProducers) {
                eventProducer.after();
            }
            
            EntityManager em = entityManager;
            TypedQuery<LastDeviceEventTime> lastEventTimes = em
                    .createQuery(String.format("SELECT NEW %s(x.device.id, max(x.time)) "
                                             + "FROM DeviceEvent x "
                                             + "WHERE x.type in (:validTypes)"
                                             + "GROUP BY x.device.id", 
                            LastDeviceEventTime.class.getCanonicalName()), 
                            LastDeviceEventTime.class);
            lastEventTimes
                    .setParameter("validTypes", EnumSet.of(GEO_FENCE_ENTER, GEO_FENCE_EXIT, OVERSPEED));
            for(LastDeviceEventTime ev : lastEventTimes.getResultList()) {
                Device d = em.find(Device.class, ev.getDeviceId());
                for(User u : d.getUsers()) {
                    UserDeviceStatus.IdClass id = new UserDeviceStatus.IdClass(u, d);
                    UserDeviceStatus status = em.find(UserDeviceStatus.class, id);
                    if(status == null)
                        status = new UserDeviceStatus(id);
                    if(status.getLastCheck() == null 
                            || status.getLastCheck().before(ev.getTime())) {
                        status.setUnreadAlarms(true);
                        em.persist(status);
                    }
                }
            }
        }
    }

    public static class GeoFenceDetector extends EventProducer {
        Set<GeoFence> geoFences = new HashSet<>();
        GeoFenceCalculator geoFenceCalculator;

        @Override
        void before() {
            List<GeoFence> gfs = entityManager
                    .createQuery("SELECT g FROM GeoFence g LEFT JOIN FETCH g.devices", GeoFence.class)
                    .getResultList();
            for(GeoFence gf : gfs) {
                if(!gf.points().isEmpty())
                    geoFences.add(gf);
            }
            if (geoFences.isEmpty()) {
                return;
            }
            geoFenceCalculator = new GeoFenceCalculator(geoFences);
        }
        
        @Override
        void positionScanned(Position prevPosition, Position position) {
            if (geoFences.isEmpty()) {
                return;
            }

            Device device = position.getDevice();
            // calculate
            for (GeoFence geoFence : geoFences) {
                if (prevPosition != null) {
                    boolean containsCurrent = geoFenceCalculator.contains(geoFence, position);
                    boolean containsPrevious = geoFenceCalculator.contains(geoFence, prevPosition);

                    DeviceEventType eventType = null;
                    if (containsCurrent && !containsPrevious) {
                        eventType = DeviceEventType.GEO_FENCE_ENTER;
                    } else if (!containsCurrent && containsPrevious) {
                        eventType = DeviceEventType.GEO_FENCE_EXIT;
                    }

                    if (eventType != null) {
                        DeviceEvent event = new DeviceEvent();
                        event.setTime(currentDate());
                        event.setDevice(device);
                        event.setType(eventType);
                        event.setPosition(position);
                        event.setGeoFence(geoFence);
                        entityManager.persist(event);
                    }
                }
            }
        }

        @Override
        void after() {
            geoFences.clear();
            geoFenceCalculator = null;
        }
    }

    public static class OdometerUpdater extends EventProducer {
        Map<Device, List<Maintenance>> maintenances = new HashMap<>();
        
        @Override
        void before() {
            List<Device> devices = entityManager().createQuery("SELECT d FROM Device d WHERE d.autoUpdateOdometer=:b", Device.class)
                    .setParameter("b", Boolean.TRUE).getResultList();
            if (devices.isEmpty()) {
                return;
            }

            // load maintenances
            for (Maintenance maintenance : entityManager().createQuery("SELECT m FROM Maintenance m WHERE m.device IN :devices", Maintenance.class)
                    .setParameter("devices", devices).getResultList()) {
                List<Maintenance> deviceMaintenances = maintenances.get(maintenance.getDevice());
                if (deviceMaintenances == null) {
                    deviceMaintenances = new LinkedList<>();
                    maintenances.put(maintenance.getDevice(), deviceMaintenances);
                }
                deviceMaintenances.add(maintenance);
            }
        }
        
        @Override
        void positionScanned(Position prevPosition, Position position) {
            Device device = position.getDevice();
            if (device.isAutoUpdateOdometer() && prevPosition != null) {
                double distance = GeoUtils.getDistance(
                        prevPosition.getLongitude(), prevPosition.getLatitude(),
                        position.getLongitude(), position.getLatitude());

                if (distance > 0.003) {
                    double prevOdometer = device.getOdometer();
                    device.setOdometer(prevOdometer + distance);
                    // post maintenance overdue events
                    List<Maintenance> deviceMaintenances = maintenances.get(device);
                    if (deviceMaintenances != null) {
                        for (Maintenance maintenance : deviceMaintenances) {
                            double serviceThreshold = maintenance.getLastService() + maintenance.getServiceInterval();
                            if (prevOdometer < serviceThreshold && device.getOdometer() >= serviceThreshold) {
                                DeviceEvent event = new DeviceEvent();
                                event.setTime(currentDate());
                                event.setDevice(device);
                                event.setType(DeviceEventType.MAINTENANCE_REQUIRED);
                                event.setPosition(position);
                                event.setMaintenance(maintenance);
                                entityManager().persist(event);
                            }
                        }
                    }
                }
            }
        }

        @Override
        void after() {
            maintenances.clear();
        }
    }

    public static class OverspeedDetector extends EventProducer {
        @Override
        void before() {
        }

        @Override
        void positionScanned(Position prevPosition, Position position) {
            Device device = position.getDevice();
            if (position.getSpeed() == null || device.getSpeedLimit() == null
                    || device.getSpeedLimit() < 0.99) {
                return;
            }

            if (position.getSpeed() > device.getSpeedLimit() &&
                    (prevPosition == null
                    || prevPosition.getSpeed() == null
                    || prevPosition.getSpeed() <= device.getSpeedLimit())) {
                DeviceEvent overspeedEvent = new DeviceEvent();
                overspeedEvent.setTime(currentDate());
                overspeedEvent.setDevice(device);
                overspeedEvent.setType(DeviceEventType.OVERSPEED);
                overspeedEvent.setPosition(position);
                entityManager().persist(overspeedEvent);
            }
        }

        @Override
        void after() {
        }
    }

    public static class StopMoveDetector extends EventProducer {
        Map<Long, Long> previousIdlePositions;

        @Override
        void before() {
            if (previousIdlePositions == null) {
                previousIdlePositions = new HashMap<>();
            }
        }

        @Override
        void positionScanned(Position prevPosition, Position position) {
            Device device = position.getDevice();

            if (isIdle(position)) {
                if (previousIdlePositions.containsKey(device.getId())) {
                    if (prevPosition != null && !isIdleForMinimumTime(prevPosition) && isIdleForMinimumTime(position)) {
                        DeviceEvent stopEvent = new DeviceEvent();
                        stopEvent.setTime(currentDate());
                        stopEvent.setDevice(device);
                        stopEvent.setType(DeviceEventType.STOPPED);
                        Long previousIdlePositionId = previousIdlePositions.get(device.getId());
                        stopEvent.setPosition(entityManager().find(Position.class, previousIdlePositionId));
                        entityManager().persist(stopEvent);
                    }
                } else {
                    previousIdlePositions.put(device.getId(), position.getId());
                }
            } else {
                if (isIdleForMinimumTime(position)) {
                    DeviceEvent movingEvent = new DeviceEvent();
                    movingEvent.setTime(currentDate());
                    movingEvent.setDevice(device);
                    movingEvent.setType(DeviceEventType.MOVING);
                    movingEvent.setPosition(position);
                    entityManager().persist(movingEvent);
                }
                previousIdlePositions.remove(device.getId());
            }
        }

        @Override
        void after() {
        }

        private boolean isIdle(Position position) {
            return position.getSpeed() == null || position.getSpeed() <= position.getDevice().getIdleSpeedThreshold();
        }

        private boolean isIdleForMinimumTime(Position position) {
            Device device = position.getDevice();
            Long previousIdlePositionId = previousIdlePositions.get(device.getId());
            Position previousIdlePosition = previousIdlePositionId == null
                    ? null : entityManager().find(Position.class, previousIdlePositionId);
            long minIdleTime = (long) device.getMinIdleTime() * 1000;
            return previousIdlePosition != null
                    && position.getTime().getTime() - previousIdlePosition.getTime().getTime() > minIdleTime;
        }
    }
    
    public static class RoutesDetector extends EventProducer {
        GeoFenceCalculator gfCalc;
        Map<Route, List<RoutePoint>> unvisited = new HashMap<>(); 
        
        @Override
        void before() {
            Map<Long, GeoFence> gfs = new HashMap<>();
            List<Route> routes = entityManager.createQuery("SELECT r FROM Route r "
                    + "LEFT JOIN FETCH r.routePoints "
                    + "WHERE r.device IS NOT NULL AND r.status IN (:status)", Route.class)
                    .setParameter("status", EnumSet.of(Route.Status.NEW, Route.Status.IN_PROGRESS_OK, Route.Status.IN_PROGRESS_LATE))
                    .getResultList();
            for(Route r : routes) {
                if(r.getRoutePoints().get(0).getDeadline() == null)
                    continue;
                unvisited.put(r, new ArrayList<>());
                for(int i=0;i<r.getRoutePoints().size();++i) {
                    RoutePoint rp = r.getRoutePoints().get(i);
                    if(rp.getEnterTime() == null || rp.getExitTime() == null) {
                        long id = rp.getGeofence().getId();
                        if(!gfs.containsKey(id)) {
                            GeoFence gf = new GeoFence().copyFrom(rp.getGeofence());
                            gf.setDevices(new HashSet<>());
                            gfs.put(id, gf);  
                        }
                        gfs.get(id).getDevices().add(r.getDevice());
                        unvisited.get(r).add(rp);
                    }
                }
            }
            if (gfs.isEmpty()) {
                return;
            }
            gfCalc = new GeoFenceCalculator(gfs.values());
        }

        @Override
        void positionScanned(Position prevPosition, Position position) {            
            for(Route route : unvisited.keySet()) {
                Date start = new Date(route.getRoutePoints().get(0).getDeadline().getTime() - route.getTolerance()*60*1000);
                if(position.getTime().before(start))
                    continue;
                List<RoutePoint> activePoints = new ArrayList<>(unvisited.get(route));
                if(route.isForceFirst() && route.getStatus() == Route.Status.NEW) {
                    activePoints = Collections.singletonList(route.getRoutePoints().get(0));
                } else if(route.isForceLast() && route.getDonePointsCount() != route.getRoutePoints().size()-1) {
                    activePoints.remove(activePoints.size()-1);
                }
                
                for(RoutePoint rp : activePoints) {
                    GeoFence gf = new GeoFence().copyFrom(rp.getGeofence());
                    gf.setDevices(Collections.singleton(route.getDevice()));
                    boolean beforeEnter = rp.getEnterTime() == null;
                    if(route.isForceFirst() && route.getStatus() == Route.Status.NEW)
                        beforeEnter = false;
                    if (prevPosition != null) {
                        boolean containsCurrent = gfCalc.contains(gf, position);
                        boolean containsPrevious = gfCalc.contains(gf, prevPosition);

                        if (containsCurrent && !containsPrevious && beforeEnter) {
                            rp.setEnterTime(position.getTime());
                            entityManager.persist(rp);
                            updateStatus(route, rp);
                        } else if (!containsCurrent && containsPrevious && !beforeEnter) {
                            unvisited.get(route).remove(rp);
                            rp.setExitTime(position.getTime());
                            entityManager.persist(rp);
                            updateStatus(route, rp);
                        }
                    }
                }
            }
        }
        
        public void updateStatus(Route route, RoutePoint rp) {
            Date alarm = new Date(rp.getDeadline().getTime() + route.getTolerance()*60*1000);
            Date time = rp.getEnterTime();
            if(route.getStatus() == Route.Status.NEW && route.isForceFirst())
                time = rp.getExitTime();
            
            if(route.getDonePointsCount() == route.getRoutePoints().size()) {
                if(time.after(alarm))
                    route.setStatus(Route.Status.FINISHED_LATE);
                else
                    route.setStatus(Route.Status.FINISHED_OK);
            } else {
                if(time.after(alarm))
                    route.setStatus(Route.Status.IN_PROGRESS_LATE);
                else
                    route.setStatus(Route.Status.IN_PROGRESS_OK);
            }
        }

        @Override
        void after() {
            unvisited.clear();
            gfCalc = null;
        }
    }
    
    public static class RoutesArchivizer extends EventProducer {

        @Override
        void before() {
            List<Route> routes = entityManager.createQuery("SELECT r FROM Route r "
                    + "LEFT JOIN FETCH r.routePoints "
                    + "WHERE r.device IS NOT NULL AND r.status IN (:status) AND archive = :false", Route.class)
                    .setParameter("status", EnumSet.of(Route.Status.FINISHED_OK, Route.Status.FINISHED_LATE, Route.Status.CANCELLED))
                    .setParameter("false", false)
                    .getResultList();
            for(Route r : routes) {
                if(r.getArchiveAfter() <= 0)
                    continue;
                Date finish = new Date(System.currentTimeMillis() - r.getArchiveAfter()*24*60L*60*1000);
                if(r.getCancelTimestamp() != null && finish.after(r.getCancelTimestamp()))
                    r.setArchived(true);
                else {
                    List<RoutePoint> rps = r.getRoutePoints();
                    RoutePoint last = rps.get(rps.size()-1);
                    Date lastTime;
                    if(r.isForceLast())
                        lastTime = last.getEnterTime();
                    else
                        lastTime = last.getExitTime();
                    if(finish.after(lastTime))
                        r.setArchived(true);
                }
                entityManager.persist(r);
            }
        }

        @Override
        void positionScanned(Position prevPosition, Position position) {
        }

        @Override
        void after() {
        }
        
    }
    
    private OfflineDetector offlineDetector;
    private GeoFenceDetector geoFenceDetector;
    private OdometerUpdater odometerUpdater;
    private OverspeedDetector overspeedDetector;
    private StopMoveDetector stopMoveDetector;
    private PositionScanner positionScanner;
    private RoutesDetector routesDetector;
    private RoutesArchivizer routesArchivizer;

    private ScheduledExecutorService executor;
    
    private ApplicationSettings applicationSettings;
    
    public EventDaemon() {        
        offlineDetector = new OfflineDetector();
        geoFenceDetector = new GeoFenceDetector();
        odometerUpdater = new OdometerUpdater();
        overspeedDetector = new OverspeedDetector();
        stopMoveDetector = new StopMoveDetector();
        positionScanner = new PositionScanner();
        routesDetector = new RoutesDetector();
        routesArchivizer = new RoutesArchivizer();
        
        positionScanner.eventProducers.add(geoFenceDetector);
        positionScanner.eventProducers.add(odometerUpdater);
        positionScanner.eventProducers.add(overspeedDetector);
        positionScanner.eventProducers.add(stopMoveDetector);
        positionScanner.eventProducers.add(routesDetector);
        positionScanner.eventProducers.add(routesArchivizer);
    }

    public void start() {
        executor = Executors.newScheduledThreadPool(1);
        startTasks();
    }

    private synchronized void startTasks() {
        for (final Runnable task : new Runnable[] { offlineDetector, positionScanner }) {
            executor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        task.run();
                    } catch(Exception e) {
                        Logger logger = LoggerFactory.getLogger(Application.class);
                        logger.error("EventDaemon:", e);
                    }
                }
            }, 0, 1, TimeUnit.MINUTES);
        }
    }
    
    public void stop() {
        executor.shutdown();
        executor = null;
    }
}
