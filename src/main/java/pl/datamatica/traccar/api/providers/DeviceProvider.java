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
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;

public class DeviceProvider {
    private final EntityManager em;
    private User requestUser;
    
    public DeviceProvider(EntityManager em) {
        this.em = em;
    }
    
    public void setRequestUser(User requestUser) {
        this.requestUser = requestUser;
    }
    
    public Device getDevice(long id) {
        return em.find(Device.class, id);
    }
    
    public Stream<Device> getAllAvailableDevices() {
        if(requestUser.getAdmin())
            return getAllDevices();
        else
            return managedAndMe().flatMap(u -> u.getDevices().stream()).distinct();
    }
    
    public boolean isVisible(Device device) {
        if(requestUser.getAdmin())
            return true;
        return managedAndMe().anyMatch((managed) -> (managed.getDevices().contains(device)));
    }
    
    private Stream<User> managedAndMe() {
        Stream<User> meStream = Stream.of(requestUser);
        if(!requestUser.getManager())
            return meStream;
        return Stream.concat(meStream, requestUser.getManagedUsers().stream());
    }
    
    private Stream<Device> getAllDevices() {
        TypedQuery<Device> tq = em.createQuery("Select x from Device x", Device.class);
        return tq.getResultList().stream();
    }
    
    public boolean isImeiValid(String imei) {
        //todo
        return true;
    }

    public Device createDevice(String imei) {
        if(!isImeiValid(imei))
            return null;
        
        em.getTransaction().begin();
        Device device = new Device();
        device.setUniqueId(imei);
        device.setUsers(Collections.singleton(requestUser));
        device.setOwner(requestUser);
        em.persist(device);
        em.getTransaction().commit();
        
        return device;
    }
}
