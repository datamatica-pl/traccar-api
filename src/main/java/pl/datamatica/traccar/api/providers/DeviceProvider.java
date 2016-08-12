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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;

public class DeviceProvider {
    private EntityManager em;
    
    public DeviceProvider(EntityManager em) {
        this.em = em;
    }
    
    public Device getDevice(long id) {
        return em.find(Device.class, id);
    }
    
    public Stream<Device> getAllAvailableDevices(User user) {
        if(user.getAdmin())
            return getAllDevices();
        else if(user.getManager()) {
            Stream<Device> availableDevices = user.getDevices().stream();
            for(User managed : user.getManagedUsers())
                availableDevices = Stream.concat(availableDevices, getAllAvailableDevices(managed));
            return availableDevices.distinct();
        } else {
            return user.getDevices().stream();
        }
    }
    
    public static boolean isVisibleToUser(Device device, User user) {
        if(user.getAdmin())
            return true;
        return managedAndMe(user).anyMatch((managed) -> (managed.getDevices().contains(device)));
    }
    
    private static Stream<User> managedAndMe(User user) {
        Stream<User> me = Stream.of(user);
        if(!user.getManager())
            return me;
        return Stream.concat(me, user.getManagedUsers().stream());
    }
    
    public Stream<Device> getAllDevices() {
        TypedQuery<Device> tq = em.createQuery("Select x from Device x", Device.class);
        return tq.getResultList().stream();
    }
}
