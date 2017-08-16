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
    
    public Stream<GeoFence> getAllAvailableGeoFences() {
        Stream<GeoFence> visible;
        if(requestUser.getAdmin())
            visible = getAllGeoFences();
        else
            visible = requestUser.getAllAvailableGeoFences().stream()
                    .map(gf -> {
                        GeoFence geo = gf.clone();
                        geo.getDevices().retainAll(requestUser.getAllAvailableDevices());
                        return geo;
                    });
        return visible.filter(gf -> !gf.isDeleted());
    }
    
    public GeoFence getGeoFence(long id) throws ProviderException {
        GeoFence geoFence = get(GeoFence.class, id, this::isVisible).clone();
        geoFence.getDevices().retainAll(requestUser.getAllAvailableDevices());
        if(!requestUser.getAdmin()) {
            HashSet<User> users = new HashSet<>(requestUser.getAllManagedUsers());
            users.add(requestUser);
            geoFence.getUsers().retainAll(users);
        }
        return geoFence;
    }

    private boolean isVisible(GeoFence gf) {
        if(requestUser.getAdmin())
            return true;
        return getAllAvailableGeoFences().anyMatch(geo -> geo.equals(gf));
    }

    public GeoFence createGeoFence(IGeoFenceInfo geoFenceDto) {
        GeoFence gf = new GeoFence();
        gf.setName(geoFenceDto.getGeofenceName());
        gf.setDescription(geoFenceDto.getDescription());
        gf.setColor(geoFenceDto.getColor());
        gf.setPoints(geoFenceDto.getPointsString());
        gf.setType(GeoFenceType.valueOf(geoFenceDto.getType()));
        if(gf.getType() != GeoFenceType.POLYGON)
            gf.setRadius(geoFenceDto.getRadius());
        gf.setUsers(Collections.singleton(requestUser));
        Set<Device> devices = Arrays.stream(geoFenceDto.getDeviceIds())
                .mapToObj(id -> em.find(Device.class, id))
                .collect(Collectors.toSet());
        if(!requestUser.getAdmin())
            devices.retainAll(requestUser.getDevices());
        gf.setDevices(devices);
        em.persist(gf);
        
        logger.info("{} created geofence {} (id={})", 
                requestUser.getLogin(), gf.getName(), gf.getId());
        return gf;
    }

    public void updateGeoFence(long id, AddGeoFenceDto geoFenceDto) throws ProviderException {
        GeoFence geoFence = get(GeoFence.class, id, this::isVisible);
        
        geoFence.setColor(geoFenceDto.getColor());
        geoFence.setDescription(geoFenceDto.getDescription());

        Set<Device> devices = Arrays.stream(geoFenceDto.getDeviceIds())
                .mapToObj(i -> em.find(Device.class, i))
                .collect(Collectors.toSet());
        if(requestUser.getAdmin())
            geoFence.getDevices().clear();
        else
            geoFence.getDevices().removeAll(requestUser.getAllAvailableDevices());
        geoFence.getDevices().addAll(devices);
        
        geoFence.setName(geoFenceDto.getGeofenceName());
        geoFence.setPoints(geoFenceDto.getPointsString());
        geoFence.setType(GeoFenceType.valueOf(geoFenceDto.getType()));
        if(geoFence.getType() != GeoFenceType.POLYGON)
            geoFence.setRadius(geoFenceDto.getRadius());
        
        em.persist(geoFence);
        logger.info("{} updated geofence {} (id={})",
                requestUser.getLogin(), geoFence.getName(), geoFence.getId());
    }
    
    public void updateGeofenceShare(long id, List<Long> uids) throws ProviderException{
        GeoFence gf = get(GeoFence.class, id, this::isVisible);
        if(requestUser.getAdmin())
            gf.getUsers().clear();
        else
            gf.getUsers().removeAll(requestUser.getAllManagedUsers());
        Set<Long> ids = new HashSet<>(uids);
        List<User> users;
        if(requestUser.getAdmin()) {
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
        boolean shouldManageTransaction = !em.getTransaction().isActive();
        if(shouldManageTransaction)
            em.getTransaction().begin();
        GeoFence gf = get(GeoFence.class, id, this::isVisible);
        if(!canDeleteGeofence(gf))
            throw new ProviderException(Type.ACCESS_DENIED);
        gf.getUsers().remove(requestUser);
        if(!gf.getUsers().isEmpty()) {
            logger.info("{} stopped seeing geofence {} (id={})",
                requestUser.getLogin(), gf.getName(), gf.getId());
        } else {
            gf.setDeleted(true);
            logger.info("{} deleted geofence {} (id={})",
                requestUser.getLogin(), gf.getName(), gf.getId());
        }
        em.persist(gf);
        if(shouldManageTransaction)
            em.getTransaction().commit();
    }

    private boolean canDeleteGeofence(GeoFence gf) {
        return gf.getUsers().contains(requestUser);
    }
}
