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
package pl.datamatica.traccar.api.controllers;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.api.Context;
import pl.datamatica.traccar.api.dtos.DeviceDto;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;

public class DevicesController {
    
    private EntityManager em;
    
    public DevicesController(Context context) {
        em = context.getEntityManager();
    }
    
    public List<DeviceDto> get(User user) {
        if(user.getAdmin()) {
            TypedQuery<Device> tq = em.createQuery("Select x from Device x", Device.class);
            return convert(tq.getResultList());
        }
        return convert(new ArrayList<>(user.getAllAvailableDevices()));
    }
    
    public DeviceDto get(long id, User user) {
        Device device = em.find(Device.class, id);
        if(!user.getAllAvailableDevices().contains(device))
            return null;
        return convert(device);
    }
    
    public List<DeviceDto> convert(List<Device> devices) {
        List<DeviceDto> result = new ArrayList<>();
        devices.stream().forEach((device) -> {
            result.add(new DeviceDto(device));
        });
        return result;
    }
    
    public DeviceDto convert(Device device) {
        return new DeviceDto(device);
    }
}
