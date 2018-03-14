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
import pl.datamatica.traccar.api.dtos.in.AddGeoFenceDto;
import pl.datamatica.traccar.api.dtos.in.EditRouteDto;
import pl.datamatica.traccar.api.dtos.in.RoutePointDto;
import pl.datamatica.traccar.model.DbRoute;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.GeoFenceType;
import pl.datamatica.traccar.model.Route;
import pl.datamatica.traccar.model.RoutePoint;

/**
 *
 * @author ŁŁ
 */
public class RouteProvider extends ProviderBase {
    private final String SELECT_WITH_JOINS = "select distinct r from DbRoute r left join fetch r.device"
            + " left join fetch r.routePoints rp left join fetch rp.geofence"
            + " left join fetch r.corridor";
    
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
    
    public Stream<DbRoute> getAllAvailableRoutes(boolean archive) throws ProviderException {
        if(!requestUser.hasPermission(UserPermission.TRACK_READ))
            throw new ProviderException(Type.ACCESS_DENIED);
        if(requestUser.hasPermission(UserPermission.ALL_TRACKS))
            return em.createQuery(SELECT_WITH_JOINS+" where r.archive = :archive", DbRoute.class)
                    .setParameter("archive", archive).getResultList().stream();
        return em.createQuery(SELECT_WITH_JOINS+" where r.owner = :user and r.archive = :archive", DbRoute.class)
                .setParameter("user", requestUser).setParameter("archive", archive)
                .getResultList().stream();
    }
    
    public void updateRoute(long id, EditRouteDto dto) throws ProviderException {
        if(!requestUser.hasPermission(UserPermission.TRACK_EDIT))
            throw new ProviderException(Type.ACCESS_DENIED);
        DbRoute r = em.find(DbRoute.class, id);
        if(r == null)
            throw new ProviderException(Type.NOT_FOUND);
        if(!requestUser.hasPermission(UserPermission.ALL_TRACKS) && !r.getOwner().equals(requestUser))
            throw new ProviderException(Type.ACCESS_DENIED);
        
        r.setArchiveAfter(dto.getArchiveAfter());
        r.setName(dto.getName());
        r.setForceFirst(dto.getForceFirst());
        r.setForceLast(dto.getForceLast());
        r.setLinePoints(dto.getPolyline());
        r.setOwner(requestUser);
        r.setTolerance(dto.getTolerance());
        if(dto.getArchive() != null)
            r.setArchived(dto.getArchive());
        if(dto.getDeviceId() != null)
            r.setDevice(devices.getEditableDevice(dto.getDeviceId()));
        if(dto.getCancel() != null && dto.getCancel()) {
            r.setStatus(Route.Status.CANCELLED);
            r.setCancelTimestamp(new Date());
        }
        
        r.getRoutePoints().clear();
        List<GeoFence> gfs = new ArrayList<>();
        for(AddGeoFenceDto agf : dto.getNewGeofences()){
            GeoFence gf = new GeoFence();
            gf.setUsers(Collections.singleton(requestUser));
            gf.setRouteOnly(true);
            gf.setPoints(agf.getPointsString());
            gf.setRadius(agf.getRadius());
            gf.setType(GeoFenceType.CIRCLE);
            if(agf.getAddress() != null)
                gf.setAddress(agf.getAddress());
            if(agf.getColor() != null)
                gf.setColor(agf.getColor());
            if(agf.getDescription() != null)
                gf.setDescription(agf.getDescription());
            em.persist(gf);
            gfs.add(gf);
        }
        
        int i = 0;
        for(RoutePointDto rpd : dto.getPoints()) {
            RoutePoint rp;
            if(rpd.getId() != null && rpd.getId() != 0)
                rp = em.find(RoutePoint.class, rpd.getId());
            else {
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
    }
}
