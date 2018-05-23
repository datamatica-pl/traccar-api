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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import pl.datamatica.traccar.api.dtos.in.AddGeoFenceDto;
import pl.datamatica.traccar.api.dtos.IGeoFenceInfo;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.GeoFenceType;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

public class GeoFenceProvider extends ProviderBase{
    private User requestUser;
    private Logger logger;
    
    public GeoFenceProvider(EntityManager em) {
        super(em);
        logger = DbLog.getLogger();
    }
    
    public void setRequestUser(User user) {
        this.requestUser = user;
    }
    
    public Stream<GeoFence> getAllAvailableGeoFences() throws ProviderException {
        if (!requestUser.hasPermission(UserPermission.GEOFENCE_READ))
            throw new ProviderException(Type.ACCESS_DENIED);
        
        return getAvailableGeoFences();
    }
    
    private  Stream<GeoFence> getAvailableGeoFences() {
        Stream<GeoFence> visible;
        if(requestUser.hasPermission(UserPermission.ALL_GEOFENCES))
            visible = getAllGeoFences();
        else if (requestUser.hasPermission(UserPermission.GEOFENCE_READ))
            visible = requestUser.getAllAvailableGeoFences().stream()
                    .map(gf -> {
                        GeoFence geo = gf.clone();
                        geo.getDevices().retainAll(requestUser.getAllAvailableDevices());
                        return geo;
                    });
        else
            return Collections.EMPTY_LIST.stream();
            
        return visible.filter(gf -> !gf.isDeleted());
    }
    
    public GeoFence getGeoFence(long id) throws ProviderException {
        if (!requestUser.hasPermission(UserPermission.GEOFENCE_READ))
            throw new ProviderException(Type.ACCESS_DENIED);
        
        GeoFence geoFence = get(GeoFence.class, id, this::isVisible).clone();
        geoFence.getDevices().retainAll(requestUser.getAllAvailableDevices());
        if(!requestUser.hasPermission(UserPermission.ALL_USERS)) {
            HashSet<User> users = new HashSet<>(requestUser.getAllManagedUsers());
            users.add(requestUser);
            geoFence.getUsers().retainAll(users);
        }
        return geoFence;
    }
    
    GeoFence getEditableGeofence(long id) throws ProviderException {
        if(!requestUser.hasPermission(UserPermission.GEOFENCE_READ))
            throw new ProviderException(Type.ACCESS_DENIED);
        return get(GeoFence.class, id, this::isVisible);
    }

    private boolean isVisible(GeoFence gf) {
        if(requestUser.hasPermission(UserPermission.ALL_GEOFENCES))
            return true;
        if (!requestUser.hasPermission(UserPermission.GEOFENCE_READ))
            return false;
        return getAvailableGeoFences().anyMatch(geo -> geo.equals(gf));
    }

    public GeoFence createGeoFence(IGeoFenceInfo geoFenceDto) throws ProviderException {
        if (!requestUser.hasPermission(UserPermission.GEOFENCE_EDIT))
            throw new ProviderException(Type.ACCESS_DENIED);
        
        GeoFence gf = new GeoFence();
        updateGeoFence(gf, geoFenceDto);
        gf.setUsers(Collections.singleton(requestUser));
        gf.setOwner(requestUser);
        Set<Device> devices = Arrays.stream(geoFenceDto.getDeviceIds())
                .mapToObj(id -> em.find(Device.class, id))
                .collect(Collectors.toSet());
        if(!requestUser.hasPermission(UserPermission.ALL_DEVICES))
            devices.retainAll(requestUser.getDevices());
        gf.setDevices(devices);
        em.persist(gf);
        
        logger.info("{} created geofence {} (id={})", 
                requestUser.getLogin(), gf.getName(), gf.getId());
        return gf;
    }

    public void updateGeoFence(long id, AddGeoFenceDto geoFenceDto) throws ProviderException {
        if (!requestUser.hasPermission(UserPermission.GEOFENCE_EDIT))
            throw new ProviderException(Type.ACCESS_DENIED);
        
        GeoFence geoFence = get(GeoFence.class, id, this::isVisible);
        updateGeoFence(geoFence, geoFenceDto);
        
        Set<Device> devices = Arrays.stream(geoFenceDto.getDeviceIds())
                .mapToObj(i -> em.find(Device.class, i))
                .collect(Collectors.toSet());
        if(requestUser.hasPermission(UserPermission.ALL_DEVICES))
            geoFence.getDevices().clear();
        else
            geoFence.getDevices().removeAll(requestUser.getAllAvailableDevices());
        geoFence.getDevices().addAll(devices);
        
        em.persist(geoFence);
        logger.info("{} updated geofence {} (id={})",
                requestUser.getLogin(), geoFence.getName(), geoFence.getId());
    }
    
    private void updateGeoFence(GeoFence gf, IGeoFenceInfo dto) {
        gf.setName(dto.getGeofenceName());
        gf.setDescription(dto.getDescription());
        gf.setColor(dto.getColor());
        gf.setPoints(dto.getPointsString());
        gf.setType(GeoFenceType.valueOf(dto.getType()));
        if(gf.getType() != GeoFenceType.POLYGON)
            gf.setRadius(dto.getRadius());
        gf.setAddress(dto.getAddress());
    }
    
    public void updateGeofenceShare(long id, List<Long> uids) throws ProviderException {
        if (!requestUser.hasPermission(UserPermission.GEOFENCE_SHARE))
            throw new ProviderException(Type.ACCESS_DENIED);
        
        GeoFence gf = get(GeoFence.class, id, this::isVisible);
        if(requestUser.hasPermission(UserPermission.ALL_USERS))
            gf.getUsers().clear();
        else {
            gf.getUsers().removeAll(requestUser.getAllManagedUsers());
            gf.getUsers().remove(requestUser);
        }
        Set<Long> ids = new HashSet<>(uids);
        List<User> users;
        if(requestUser.hasPermission(UserPermission.ALL_USERS)) {
            TypedQuery<User> tq = em.createQuery("from User u where u.id in :ids", User.class);
            tq.setParameter("ids", uids);
            users = tq.getResultList();
        } else {
            users = new ArrayList<>(requestUser.getManagedUsers());
            users.add(requestUser);
            users.removeIf(u -> !ids.contains(u.getId()));
        }
        gf.getUsers().addAll(users);
    }

    private Stream<GeoFence> getAllGeoFences() {
        TypedQuery<GeoFence> tq = em.createQuery("Select x from GeoFence x", GeoFence.class);
        return tq.getResultList().stream();
    }
    
    public void delete(long id) throws ProviderException {
        if (!requestUser.hasPermission(UserPermission.GEOFENCE_READ))
            throw new ProviderException(Type.ACCESS_DENIED);
        
        boolean shouldManageTransaction = !em.getTransaction().isActive();
        if(shouldManageTransaction)
            em.getTransaction().begin();
        GeoFence gf = get(GeoFence.class, id, this::isVisible);
        if(!canDeleteGeofence(gf))
            throw new ProviderException(Type.ACCESS_DENIED);
        
        if(gf.getOwner().equals(requestUser) || requestUser.hasPermission(UserPermission.ALL_GEOFENCES)) {
            gf.setDeleted(true);
            gf.setUsers(new HashSet<>());
            logger.info("{} deleted geofence {} (id={})",
                requestUser.getLogin(), gf.getName(), gf.getId());
        } 
        else {
            gf.getUsers().remove(requestUser);
            logger.info("{} stopped seeing geofence {} (id={})",
                requestUser.getLogin(), gf.getName(), gf.getId());
        }
        em.persist(gf);
        if(shouldManageTransaction)
            em.getTransaction().commit();
    }

    private boolean canDeleteGeofence(GeoFence gf) {
        if (requestUser.hasPermission(UserPermission.ALL_GEOFENCES)) {
            return true;
        }
        return gf.getUsers().contains(requestUser);
    }
}
