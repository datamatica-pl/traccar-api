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
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.DeviceEvent;
import pl.datamatica.traccar.model.DeviceEventType;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

public class AlertProvider {
    private final EntityManager em;
    private final User requestUser;
    private final DeviceProvider devices;
    private final GeoFenceProvider geofences;
    
    public AlertProvider(EntityManager em, User requestUser) {
        this.em = em;
        this.requestUser = requestUser;
        this.devices = new DeviceProvider(em, requestUser, null, null, null);
        this.geofences = new GeoFenceProvider(em);
        geofences.setRequestUser(requestUser);
    }
    
    public List<DeviceEvent> getAllAvailableAlerts() throws ProviderException {
        if (!requestUser.hasPermission(UserPermission.ALERTS_READ))
            throw new ProviderException(ProviderException.Type.ACCESS_DENIED);
        
        List<Device> validDevices = devices.getAllAvailableDevices()
                .filter(d -> !d.isBlocked() && !d.isDeleted())
                .collect(Collectors.toList());
        
        List<GeoFence> validGeofences = geofences.getAllAvailableGeoFences()
                .filter(g -> !g.isDeleted())
                .collect(Collectors.toList());
        
        if(validDevices.isEmpty())
            return Collections.EMPTY_LIST;
        
        List<DeviceEvent> events;
        
        String query = "Select de from DeviceEvent de\n"
                + "left join fetch de.position\n"
                + "left join fetch de.geoFence\n"
                + "left join fetch de.maintenance\n"
                + "where de.device in (:devices) and\n"
                + "de.type in (:valid_types)\n";
        if(validGeofences.isEmpty())
            query += "and de.type not in (:geofence_events)";
        else 
            query += "and (de.geoFence in (:geofences) or de.type not in (:geofence_events))";
        
        TypedQuery<DeviceEvent> tq = em.createQuery(query, DeviceEvent.class)
                .setParameter("devices", validDevices)
                .setParameter("geofence_events", 
                        EnumSet.of(DeviceEventType.GEO_FENCE_ENTER,
                        DeviceEventType.GEO_FENCE_EXIT))
                .setParameter("valid_types", 
                        EnumSet.of(DeviceEventType.GEO_FENCE_ENTER,
                                DeviceEventType.GEO_FENCE_EXIT,
                                DeviceEventType.OVERSPEED,
                                DeviceEventType.MAINTENANCE_REQUIRED));
        
        if(!validGeofences.isEmpty())
            tq.setParameter("geofences", validGeofences);
        events = tq.getResultList();
                
        return events.stream()
                .filter(e -> ChronoUnit.DAYS.between(e.getTime().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate(), 
                        LocalDate.now()) <= e.getDevice().getAlertsHistoryLength())
                .filter(e -> e.getPosition().getValid() != null 
                        && e.getPosition().getValid())
                .filter(e -> e.getPosition().getSpeed() == null 
                        || e.getPosition().getSpeed() < 255)
                .collect(Collectors.toList());
    }
}
