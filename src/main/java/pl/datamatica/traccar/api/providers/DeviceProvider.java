/*
 *  Copyright (C) 2016  Datamatica (dev@datamatica.pl)
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published
 *  by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pl.datamatica.traccar.api.providers;

import com.google.gson.JsonArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import pl.datamatica.traccar.api.dtos.in.EditDeviceDto;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Maintenance;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserDeviceStatus;
import com.google.gson.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.model.RegistrationMaintenance;

public class DeviceProvider extends ProviderBase {
    private User requestUser;
    private ImeiProvider imeis;
    private DeviceGroupProvider groups;
    private Logger logger;
    private final SimpleDateFormat dateFormat;
    
    public DeviceProvider(EntityManager em, User requestUser, ImeiProvider imeis,
            DeviceGroupProvider groups) {
        super(em);
        this.requestUser = requestUser;
        this.imeis = imeis;
        this.groups = groups;
        logger = DbLog.getLogger();
        dateFormat = new SimpleDateFormat(Application.DATE_FORMAT);
    }
    
    public Device getDevice(long id) throws ProviderException {
        return get(Device.class, id, this::isVisible);
    }
    
    public Device getDeviceByImei(String imei) {
        TypedQuery<Device> tq = em.createQuery("Select x from Device x where x.uniqueId = :imei", Device.class);
        tq.setParameter("imei", imei);
        List<Device> devices = tq.getResultList();
        if(devices.isEmpty())
            return null;
        return devices.get(0);
    }
    
    public Stream<Device> getAllAvailableDevices() {
        List<Device> devices = null;
        if(requestUser.getAdmin())
            devices = getAllDevices();
        else
            devices = new ArrayList<>(requestUser.getAllAvailableDevices());
        
        loadAlarmStatus(devices);
        loadMaintenances(devices);
        loadRegistrations(devices);
        
        return devices.stream();
    }

    private void loadMaintenances(List<Device> devices) {
        List<Maintenance> maintenaces = em.createQuery(
                "SELECT m FROM Maintenance m WHERE m.device IN :devices ORDER BY m.indexNo ASC", Maintenance.class)
                .setParameter("devices", devices)
                .getResultList();
        for (Maintenance maintenance : maintenaces) {
            Device device = maintenance.getDevice();
            if (device.getMaintenances() == null) {
                device.setMaintenances(new ArrayList<>());
            }
            device.getMaintenances().add(maintenance);
        }
    }
    
    private void loadRegistrations(List<Device> devices) {
        List<RegistrationMaintenance> ms = em.createQuery(
                "SELECT m FROM RegistrationMaintenance m WHERE m.device IN :devices ORDER BY m.indexNo ASC", RegistrationMaintenance.class)
                .setParameter("devices", devices)
                .getResultList();
        for(RegistrationMaintenance m : ms) {
            Device d = m.getDevice();
            if(d.getRegistrations() == null)
                d.setRegistrations(new ArrayList<>());
            d.getRegistrations().add(m);
        }
    }

    private void loadAlarmStatus(List<Device> devices) {
        TypedQuery<UserDeviceStatus> alarmQuery = em.createQuery(
                "FROM UserDeviceStatus x "
                        + "WHERE x.id.user = :user AND x.id.device in (:devices)", UserDeviceStatus.class);
        alarmQuery.setParameter("user", requestUser);
        alarmQuery.setParameter("devices", devices);
        Map<Device, UserDeviceStatus> statesMap = new HashMap<>();
        for(UserDeviceStatus x : alarmQuery.getResultList())
            statesMap.put(x.getDevice(), x);
        for(Device d : devices) {
            UserDeviceStatus status = statesMap.get(d);
            if(status != null) {
                d.setUnreadAlarms(status.hasUnreadAlarms());
                d.setLastAlarmsCheck(status.getLastCheck());
            }
        }
    }

    public Device createDevice(String imei) throws ProviderException {
        if(!isImeiValid(imei))
            throw new ProviderException(Type.INVALID_IMEI);
        
        Device existing = getDeviceByImei(imei);
        if(existing != null) {
            if(!existing.isDeleted())
                throw new ProviderException(Type.DEVICE_ALREADY_EXISTS);
            hardDelete(existing);
        }
        
        Device device = new Device();
        device.setName(createGpsName());
        device.setUniqueId(imei);
        device.setUsers(Collections.singleton(requestUser));
        device.setIconId(4L);
        device.setOwner(requestUser);
        em.persist(device);
        
        logger.info("{} created device {} (id={})", 
                requestUser.getLogin(), device.getName(), device.getId());
        
        return device;
    }

    private static String createGpsName() {
        Random random = new Random();
        return GPS_NAME_PREFIX+(random.nextInt(99)+1);
    }
    private static final String GPS_NAME_PREFIX = "gps-";
    
    public void delete(long id) throws ProviderException {
        boolean shouldManageTransaction = !em.getTransaction().isActive();
        if(shouldManageTransaction)
            em.getTransaction().begin();
        Device device = getDevice(id);
        if(!isVisible(device))
            throw new ProviderException(Type.ACCESS_DENIED);
        if(device.isDeleted())
            throw new ProviderException(Type.ALREADY_DELETED);
        if(representsOwner(device)) {
            logger.info("{} deleted device {} (id={})",
                    requestUser.getLogin(), device.getName(), device.getId());
            device.setDeleted(true);
        } else {
            logger.info("{} stopped seeing {} (id={})",
                    requestUser.getLogin(), device.getName(), device.getId());
            device.getUsers().remove(requestUser);
        }
        em.persist(device);
        if(shouldManageTransaction)
            em.getTransaction().commit();
    }

    private boolean representsOwner(Device device) {
        return requestUser.getAdmin() 
               || requestUser.equals(device.getOwner())
               || requestUser.getAllManagedUsers().contains(device.getOwner());
    }
    
    private boolean isVisible(Device device) {
        if(requestUser.getAdmin())
            return true;
        return getAllAvailableDevices().anyMatch(d -> d.equals(device));
    }
    
    private List<Device> getAllDevices() {
        TypedQuery<Device> tq = em.createQuery("Select x from Device x", Device.class);
        return tq.getResultList();
    }
    
    private boolean isImeiValid(String imei) {
        return imeis.isImeiRegistered(imei);
    }
    
    private void hardDelete(Device device) {
        device.getUsers().clear();
        device.setLatestPosition(null);
        em.flush();
        
        Query query = em.createQuery("DELETE FROM DeviceEvent x WHERE x.device = :device");
        query.setParameter("device", device);
        query.executeUpdate();
        
        em.createQuery("DELETE FROM UserDeviceStatus x WHERE x.id.device = :device")
                .setParameter("device", device).executeUpdate();

        query = em.createQuery("DELETE FROM Position x WHERE x.device = :device");
        query.setParameter("device", device);
        query.executeUpdate();

        query = em.createQuery("SELECT g FROM GeoFence g WHERE :device MEMBER OF g.devices");
        query.setParameter("device", device);
        for (GeoFence geoFence : (List<GeoFence>) query.getResultList()) {
            geoFence.getDevices().remove(device);
        }
        em.flush();

        query = em.createQuery("DELETE FROM Maintenance x WHERE x.device = :device");
        query.setParameter("device", device);
        query.executeUpdate();

        query = em.createQuery("DELETE FROM Sensor x WHERE x.device = :device");
        query.setParameter("device", device);
        query.executeUpdate();

        query = em.createQuery("SELECT x FROM Report x WHERE :device MEMBER OF x.devices");
        query.setParameter("device", device);
        List<Report> reports = query.getResultList();
        for (Report report : reports) {
            report.getDevices().remove(device);
        }
        
        query = em.createNativeQuery("Delete from devices where id = ?");
        query.setParameter(1, device.getId());
        query.executeUpdate();
    }

    public static final Double NauticMilesToKilometersMultiplier = 0.54;
    
    public void updateDevice(long id, EditDeviceDto dto) throws ProviderException {
        Device device = getDevice(id);
        
        device.setName(dto.getDeviceName());
        device.setDeviceModelId(dto.getDeviceModelId());
        device.setIconId(dto.getIconId());
        device.setCustomIconId(dto.getCustomIconId());
        device.setColor(dto.getColor());
        device.setPhoneNumber(dto.getPhoneNumber());
        device.setPlateNumber(dto.getPlateNumber());
        device.setDescription(dto.getDescription());
        
        if(dto.getSpeedLimit() != null)
            device.setSpeedLimit(dto.getSpeedLimit() * NauticMilesToKilometersMultiplier);
        else
            device.setSpeedLimit(null);
        
        device.setFuelCapacity(dto.getFuelCapacity());
        
        em.persist(device);
        
        logger.info("{} updated device {} (id={})", 
                requestUser.getEmail(), device.getName(), device.getId());
    }

    public void applyPatch(long id, JsonObject changes) throws ProviderException {
        Device d = getDevice(id);
        if(changes.has("deviceName"))
            d.setName(changes.get("deviceName").getAsString());
        if(changes.has("deviceModelId"))
            d.setDeviceModelId(changes.get("deviceModelId").getAsLong());
        if(changes.has("iconId")) {
            if(changes.get("iconId").isJsonNull())
                d.setIconId(null);
            else
                d.setIconId(changes.get("iconId").getAsLong());
        }
        if(changes.has("customIconId")) {
            if(changes.get("customIconId").isJsonNull())
                d.setCustomIconId(null);
            else
                d.setCustomIconId(changes.get("customIconId").getAsLong());
        }
        if(changes.has("color"))
            d.setColor(changes.get("color").getAsString());
        if(changes.has("phoneNumber")) {
            if(changes.get("phoneNumber").isJsonNull())
                d.setPhoneNumber(null);
            else
                d.setPhoneNumber(changes.get("phoneNumber").getAsString());
        }
        if(changes.has("plateNumber")) {
            if(changes.get("plateNumber").isJsonNull())
                d.setPlateNumber(null);
            else
                d.setPlateNumber(changes.get("plateNumber").getAsString());
        }
        if(changes.has("description")) {
            if(changes.get("description").isJsonNull())
                d.setDescription(null);
            else
                d.setDescription(changes.get("description").getAsString());
        }
        if(changes.has("speedLimit")) {
            if(changes.get("speedLimit").isJsonNull())
                d.setSpeedLimit(null);
            else
                d.setSpeedLimit(changes.get("speedLimit").getAsDouble()*
                        DeviceProvider.NauticMilesToKilometersMultiplier);
        }
        if(changes.has("fuelCapacity")) {
            if(changes.get("fuelCapacity").isJsonNull())
                d.setFuelCapacity(null);
            else
                d.setFuelCapacity(changes.get("fuelCapacity").getAsDouble());
        }
        //web
        if(changes.has("groupId")) {
            if(changes.get("groupId").isJsonNull())
                d.setGroup(null);
            else
                d.setGroup(groups.getGroup(changes.get("groupId").getAsLong()));
        }
        if(changes.has("vehicleInfo")) {
            if(changes.get("vehicleInfo").isJsonNull())
                d.setVehicleInfo(null);
            else
                d.setVehicleInfo(changes.get("vehicleInfo").getAsString());
        }
        if(changes.has("autoUpdateOdometer")) {
            d.setAutoUpdateOdometer(changes.get("autoUpdateOdometer").getAsBoolean());
        }
        if(changes.has("timeout"))
            d.setTimeout(changes.get("timeout").getAsInt());
        if(changes.has("minIdleTime"))
            d.setMinIdleTime(changes.get("minIdleTime").getAsInt());
        if(changes.has("idleSpeedThreshold"))
            d.setIdleSpeedThreshold(changes.get("idleSpeedThreshold").getAsDouble());
        if(changes.has("timeZoneOffset")) {
            if(changes.get("timeZoneOffset").isJsonNull())
                d.setTimezoneOffset(null);
            else
                d.setTimezoneOffset(changes.get("timeZoneOffset").getAsInt());
        }
        if(changes.has("commandPassword")) {
            if(changes.get("commandPassword").isJsonNull())
                d.setCommandPassword(null);
            else
                d.setCommandPassword(changes.get("commandPassword").getAsString());
        }
        if(changes.has("historyLength") && requestUser.getAdmin())
            d.setHistoryLength(changes.get("historyLength").getAsInt());
        if(changes.has("validTo") && requestUser.getAdmin()) {
            if(changes.get("validTo").isJsonNull())
                d.setValidTo(null);
            else
                try {
                    Date date = dateFormat.parse(changes.get("validTo").getAsString());
                    d.setValidTo(date);
                } catch (ParseException ex) {
                    throw new IllegalArgumentException("Unparseable validTo");
                }
        }
        if(changes.has("showOdometer"))
            d.setShowOdometer(changes.get("showOdometer").getAsBoolean());
        if(changes.has("showProtocol"))
            d.setShowProtocol(changes.get("showProtocol").getAsBoolean());
        //
        if(changes.has("showName"))
            d.setShowName(changes.get("showName").getAsBoolean());
        if(changes.has("arrowRadius"))
            d.setIconArrowRadius(changes.get("arrowRadius").getAsDouble());
        if(changes.has("arrowMovingColor"))
            d.setIconArrowMovingColor(changes.get("arrowMovingColor").getAsString());
        if(changes.has("arrowStoppedColor"))
            d.setIconArrowStoppedColor(changes.get("arrowStoppedColor").getAsString());
        if(changes.has("arrowPausedColor"))
            d.setIconArrowPausedColor(changes.get("arrowPausedColor").getAsString());
        if(changes.has("arrowOfflineColor"))
            d.setIconArrowOfflineColor(changes.get("arrowOfflineColor").getAsString());
        
        if(changes.has("maintenances") && changes.get("maintenances").isJsonArray()) {
            JsonArray ms = changes.get("maintenances").getAsJsonArray();
            em.createQuery("DELETE FROM Maintenance m WHERE m.device = :device")
                    .setParameter("device", d).executeUpdate();
            List<Maintenance> mts = new ArrayList<>();
            for(int i=0;i<ms.size();++i) {
                Maintenance m = new Maintenance();
                JsonObject ob = ms.get(i).getAsJsonObject();
                m.setId(ob.get("id").getAsLong());
                m.setName(ob.get("name").getAsString());
                m.setLastService(ob.get("lastService").getAsDouble());
                m.setServiceInterval(ob.get("serviceInterval").getAsDouble());
                m.setIndexNo(i);
                m.setDevice(d);
                em.merge(m);
                mts.add(m);
            }
            d.setMaintenances(mts);
        }
        
        if(changes.has("registrations") && changes.get("registrations").isJsonArray()) {
            JsonArray rs = changes.get("registrations").getAsJsonArray();
            em.createQuery("DELETE FROM RegistrationMaintenance m WHERE m.device = :device")
                    .setParameter("device", d).executeUpdate();
            List<RegistrationMaintenance> rms = new ArrayList<>();
            for(int i=0;i<rs.size();++i) {
                try {
                    RegistrationMaintenance m = new RegistrationMaintenance();
                    JsonObject ob = rs.get(i).getAsJsonObject();
                    m.setId(ob.get("id").getAsLong());
                    m.setName(ob.get("name").getAsString());
                    m.setServiceDate(dateFormat.parse(ob.get("serviceDate").getAsString()));
                    m.setIndexNo(i);
                    m.setDevice(d);
                    em.merge(m);
                    rms.add(m);
                } catch (ParseException ex) {
                    //ignore
                }
            }
            d.setRegistrations(rms);
        }
        em.persist(d);
    }
}
