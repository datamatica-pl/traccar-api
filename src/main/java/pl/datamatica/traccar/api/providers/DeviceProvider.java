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

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.User;

public class DeviceProvider {
    private final EntityManager em;
    private UserProvider up;
    private User requestUser;
    
    public DeviceProvider(EntityManager em) {
        this.em = em;
        this.up = new UserProvider(em);
    }
    
    public void setRequestUser(User requestUser) {
        this.requestUser = requestUser;
        up.setRequestUser(requestUser);
    }
    
    public Device getDevice(long id) throws ProviderException {
        Device device = em.find(Device.class, id);
        if(device == null)
            throw new ProviderException(Type.NOT_FOUND);
        if(!isVisible(device))
            throw new ProviderException(Type.ACCESS_DENIED);
        return device;
    }
    
    public Device getDeviceByImei(String imei) {
        TypedQuery<Device> tq = em.createQuery("Select x from Device x where uniqueId = :imei", Device.class);
        tq.setParameter("imei", imei);
        List<Device> devices = tq.getResultList();
        if(devices.isEmpty())
            return null;
        return devices.get(0);
    }
    
    public Stream<Device> getAllAvailableDevices() {
        if(requestUser.getAdmin())
            return getAllDevices();
        else
            return managedAndMe().flatMap(u -> u.getDevices().stream()).distinct();
    }

    public Device createDevice(String imei) throws ProviderException {
        boolean shouldManageTransaction = !em.getTransaction().isActive();
        if(shouldManageTransaction)
            em.getTransaction().begin();
        if(!isImeiValid(imei))
            throw new ProviderException(Type.INVALID_IMEI);
        
        Device existing = getDeviceByImei(imei);
        if(existing != null) {
            if(!existing.isDeleted())
                throw new ProviderException(Type.IMEI_ALREADY_EXISTS);
            hardDelete(existing);
        }
        
        Device device = new Device();
        device.setUniqueId(imei);
        device.setUsers(Collections.singleton(requestUser));
        device.setOwner(requestUser);
        em.persist(device);
        if(shouldManageTransaction)
            em.getTransaction().commit();
        
        return device;
    }
    
    public void delete(long id) throws ProviderException {
        boolean shouldManageTransaction = !em.getTransaction().isActive();
        if(shouldManageTransaction)
            em.getTransaction().begin();
        Device device = getDevice(id);
        if(!canDeleteDevice(device))
            throw new ProviderException(Type.ACCESS_DENIED);
        if(device.isDeleted())
            throw new ProviderException(Type.ALREADY_DELETED);
        device.setDeleted(true);
        em.persist(device);
        if(shouldManageTransaction)
            em.getTransaction().commit();
    }

    private boolean canDeleteDevice(Device device) {
        return requestUser.getAdmin() || device.getOwner().equals(requestUser);
    }
    
    private boolean isVisible(Device device) {
        if(requestUser.getAdmin())
            return true;
        return managedAndMe().anyMatch((managed) -> (managed.getDevices().contains(device)));
    }
    
    private Stream<User> managedAndMe() {
        return up.managedAndMe(requestUser);
    }
    
    private Stream<Device> getAllDevices() {
        TypedQuery<Device> tq = em.createQuery("Select x from Device x", Device.class);
        return tq.getResultList().stream();
    }
    
    private boolean isImeiValid(String imei) {        
        //todo
        return true;
    }
    
    private void hardDelete(Device device) {
        device.getUsers().clear();
        device.setLatestPosition(null);
        em.flush();
        
        Query query = em.createQuery("DELETE FROM DeviceEvent x WHERE x.device = :device");
        query.setParameter("device", device);
        query.executeUpdate();

        query = em.createQuery("DELETE FROM Position x WHERE x.device = :device");
        query.setParameter("device", device);
        query.executeUpdate();

        query = em.createQuery("SELECT g FROM GeoFence g WHERE :device MEMBER OF g.devices");
        query.setParameter("device", device);
        for (GeoFence geoFence : (List<GeoFence>) query.getResultList()) {
            geoFence.getDevices().remove(device);
        }

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
        
        query = em.createNativeQuery("Delete from devices d where d.id = ?");
        query.setParameter(1, device.getId());
        query.executeUpdate();
    }
}
