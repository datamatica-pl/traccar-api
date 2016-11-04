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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import static java.util.concurrent.TimeUnit.DAYS;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.DeviceEvent;
import pl.datamatica.traccar.model.User;

public class AlertProvider {
    private final EntityManager em;
    private final User requestUser;
    
    public AlertProvider(EntityManager em, User requestUser) {
        this.em = em;
        this.requestUser = requestUser;
    }
    
    public List<DeviceEvent> getAllAvailableAlerts() {
        List<Device> validDevices = requestUser.getAllAvailableDevices().stream()
                .filter(d -> !d.isBlocked() && !d.isDeleted())
                .collect(Collectors.toList());
        
        if(validDevices.isEmpty())
            return Collections.EMPTY_LIST;
        
        List<DeviceEvent> events;
        
        if(requestUser.getAdmin())
            events = em.createQuery("Select de from DeviceEvent de inner join fetch de.position")
                    .getResultList();
        else
            events = em.createQuery("Select de from DeviceEvent de inner join fetch de.position\n"
                + "where de.device in (:devices)", DeviceEvent.class)
                .setParameter("devices", validDevices)
                .getResultList();
                
        return events.stream()
                .filter(e -> ChronoUnit.DAYS.between(e.getTime().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate(), 
                        LocalDate.now()) <= e.getDevice().getHistoryLength())
                .collect(Collectors.toList());
    }
}
