/*
 *  Copyright (C) 2018  Datamatica (dev@datamatica.pl)
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *  
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *  
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pl.datamatica.traccar.api.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.api.dtos.in.AddGeoFenceDto;
import pl.datamatica.traccar.api.dtos.in.EditRouteDto;
import pl.datamatica.traccar.api.dtos.in.RoutePointDto;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.api.reports.PolylineEncoder;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.GeoFence.LonLat;
import pl.datamatica.traccar.model.GeoFenceType;
import pl.datamatica.traccar.model.Route;
import pl.datamatica.traccar.model.RoutePoint;
import pl.datamatica.traccar.model.UserRequest;

/**
 *
 * @author ŁŁ
 */
public class RouteProvider extends ProviderBase {
    private final String SELECT_WITH_JOINS = "select distinct r from Route r left join fetch r.device"
            + " left join fetch r.routePoints rp left join fetch rp.geofence"
            + " left join fetch r.corridor";
    private static final int LIMIT = 250;
    
    private final User requestUser;
    private DeviceProvider devices;
    private GeoFenceProvider geofences;
    
    public RouteProvider(EntityManager em, User requestUser) {
        super(em);
        this.requestUser = requestUser;
    }
    
    public void setDeviceProvider(DeviceProvider devices) {
        this.devices = devices;
    }
    
    public void setGeofenceProvider(GeoFenceProvider geofences) {
        this.geofences = geofences;
    }
    
    public Stream<Route> getAllAvailableRoutes(boolean archive) throws ProviderException {
        if(!requestUser.hasPermission(UserPermission.TRACK_READ))
            throw new ProviderException(Type.ACCESS_DENIED);
        if(requestUser.hasPermission(UserPermission.ALL_TRACKS))
            return em.createQuery(SELECT_WITH_JOINS+" where r.archive = :archive", Route.class)
                    .setParameter("archive", archive).getResultList().stream();
        return em.createQuery(SELECT_WITH_JOINS+" where r.owner = :user and r.archive = :archive", Route.class)
                .setParameter("user", requestUser).setParameter("archive", archive)
                .getResultList().stream();
    }
    
    public Route updateRoute(long id, EditRouteDto dto) throws ProviderException {
        if(!requestUser.hasPermission(UserPermission.TRACK_EDIT))
            throw new ProviderException(Type.ACCESS_DENIED);
        Route r = get(Route.class, id, this::isVisible);
        editFromDto(r, dto);
        return r;
    }

    public Route createRoute(EditRouteDto dto) throws ProviderException {
        if(!requestUser.hasPermission(UserPermission.TRACK_EDIT))
            throw new ProviderException(Type.ACCESS_DENIED);
        Route r = new Route();
        editFromDto(r, dto);
        return r;
    }
    
    private void editFromDto(Route r, EditRouteDto dto) throws ProviderException {
        if(dto.getCancel() != null && dto.getCancel()) {
            r.setStatus(Route.Status.CANCELLED);
            r.setCancelTimestamp(new Date());
        }
        Device device = null;
        if(dto.getDeviceId() != null) {
            device = devices.getEditableDevice(dto.getDeviceId());
            if(!device.isValid(new Date())) {
                if(r.getDevice().getId() != dto.getDeviceId()
                || (r.getStatus() != Route.Status.CANCELLED && 
                    r.getStatus() != Route.Status.FINISHED_OK &&
                    r.getStatus() != Route.Status.FINISHED_LATE))
                throw new ProviderException(Type.BAD_REQUEST);
            }
            r.setDevice(device);
        } else {
            r.setDevice(null);
        }
        r.setArchiveAfter(dto.getArchiveAfter());
        r.setName(dto.getName());
        r.setForceFirst(dto.getForceFirst());
        r.setForceLast(dto.getForceLast());
        r.setLinePoints(dto.getPolyline());
        r.setOwner(requestUser);
        r.setTolerance(dto.getTolerance());
        if(dto.getArchive() != null) {
            if(r.isArchived() && !dto.getArchive()) {
                r.setArchiveAfter(0);
            }
            r.setArchived(dto.getArchive());
        }
        r.setLength(dto.getLength());
        GeoFence corr = r.getCorridor();
        if(r.getCorridor() == null) {
            corr = new GeoFence();
            corr.setType(GeoFenceType.LINE);
            corr.setUsers(Collections.singleton(requestUser));
            corr.setRouteOnly(true);
            corr.setName(r.getName()+"_c");
            corr.setOwner(requestUser);
            if(device != null)
                corr.setDevices(Collections.singleton(device));
        }
        if(dto.getCorridorWidth() != null) {
            corr.setRadius(dto.getCorridorWidth());
            StringBuilder sb = new StringBuilder();
            List<LonLat> lls = PolylineEncoder.decode(r.getLinePoints());
            for(LonLat coord : lls)
                sb.append(coord.lon).append(" ").append(coord.lat).append(",");
            if(sb.length() != 0)
                sb.replace(sb.length()-1, sb.length()-1, "");
            corr.setPoints(sb.toString());
            if(r.getDevice() != null)
                corr.setDevices(Collections.singleton(r.getDevice()));
            em.persist(corr);
            r.setCorridor(corr);
        } else {
            r.setCorridor(null);
        }
        r.getRoutePoints().removeIf(rp -> rp.getEnterTime() == null 
                && rp.getExitTime() == null);
        List<GeoFence> gfs = new ArrayList<>();
        for(AddGeoFenceDto agf : dto.getNewGeofences()){
            GeoFence gf = new GeoFence();
            gf.setUsers(Collections.singleton(requestUser));
            gf.setRouteOnly(true);
            gf.setPoints(agf.getPointsString());
            gf.setRadius(agf.getRadius());
            gf.setType(GeoFenceType.CIRCLE);
            gf.setName(agf.getGeofenceName());
            gf.setOwner(requestUser);
            if(agf.getAddress() != null)
                gf.setAddress(agf.getAddress());
            if(agf.getColor() != null)
                gf.setColor(agf.getColor());
            if(agf.getDescription() != null)
                gf.setDescription(agf.getDescription());
            em.persist(gf);
            gfs.add(gf);
        }
        int i = 0, j = r.getRoutePoints().size();
        for(;j < dto.getPoints().size();++j) {
            RoutePointDto rpd = dto.getPoints().get(j);
            RoutePoint rp;
            if(rpd.getId() != null && rpd.getId() != 0) {
                rp = em.find(RoutePoint.class, rpd.getId());
            } else {
                rp = new RoutePoint();
                GeoFence gf;
                if(rpd.getGeofenceId() != null && rpd.getGeofenceId() != 0)
                    gf = geofences.getEditableGeofence(rpd.getGeofenceId());
                else
                    gf = gfs.get(i++);
                rp.setGeofence(gf);
                rp.setDeadline(rpd.getDeadline());
            }
            r.getRoutePoints().add(rp);
        }
        em.flush();
        em.persist(r);
        deleteUnusedGeofences();
    }

    public void deleteRoute(long id) throws ProviderException {
        if(!requestUser.hasPermission(UserPermission.TRACK_EDIT))
            throw new ProviderException(Type.ACCESS_DENIED);
        Route r = get(Route.class, id, this::isVisible);
        forceDeleteRoute(r);
    }
    
    void forceDeleteRoute(Route r) {
        if(r.getCorridor() != null) {
            GeoFence corr = r.getCorridor();
            r.setCorridor(null);
            hardDeleteGeofence(corr);
        }
        em.remove(r);
        em.flush();
        deleteUnusedGeofences();
    }

    private void deleteUnusedGeofences() {
        TypedQuery<GeoFence> gfs = em.createQuery("select g from GeoFence g where"
                + " g.routeOnly = :true and g.type <> :line"
                + " and not exists (from RoutePoint rp where rp.geofence = g)", GeoFence.class)
                .setParameter("true", true).setParameter("line", GeoFenceType.LINE);
        for(GeoFence gf : gfs.getResultList())
            hardDeleteGeofence(gf);
    }
    
    private void hardDeleteGeofence(GeoFence gf) {
        em.createQuery("DELETE FROM DeviceEvent e where e.geoFence = :gf")
                .setParameter("gf", gf)
                .executeUpdate();
        gf.setDevices(null);
        gf.setUsers(null);
        em.flush();
        Query q = em.createNativeQuery("delete from geofences where id = ?");
        q.setParameter(1, gf.getId());
        q.executeUpdate();
    }
    
    public void updateLimit() {
        Date today = new Date();
        List<UserRequest> ur = em.createQuery("FROM UserRequest ur "
                + "WHERE ur.id.user = ? AND ur.id.date = ?", UserRequest.class)
                .setParameter(1, requestUser)
                .setParameter(2, today)
                .getResultList();
        if(ur.isEmpty())
            em.persist(new UserRequest(requestUser, today));
        else
            ur.get(0).increaseCount();
        em.flush();
    }
    
    public boolean isLimitReached() {
        Date today = new Date();
        List<UserRequest> ur = em.createQuery("FROM UserRequest ur "
                + "WHERE ur.id.user = ? AND ur.id.date = ?", UserRequest.class)
                .setParameter(1, requestUser)
                .setParameter(2, today)
                .getResultList();
        if(ur.isEmpty())
            return false;
        else
            return ur.get(0).getCount() > LIMIT;
    }
    
    private boolean isVisible(Route r) {
        return requestUser.hasPermission(UserPermission.ALL_TRACKS) ||
                r.getOwner().equals(requestUser);
    }
}
