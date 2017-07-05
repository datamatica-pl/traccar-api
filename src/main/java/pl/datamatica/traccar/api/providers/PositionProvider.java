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

import java.util.Date;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.User;

public class PositionProvider extends ProviderBase {
    
    private final User user;
    private final TypedQuery<Position> positionListQuery;
    
    public PositionProvider(EntityManager em, User user) {
        super(em);
        this.user = user;
        
        positionListQuery = em.createQuery("from Position p "
                + "where p.device = :device and p.time >= :minDate and (validStatus is null or validStatus = :valid) "
                + "order by p.time", Position.class);
    }
    
    public Position get(long id) throws ProviderException {
        return get(Position.class, id, this::isVisible);
    }
    
    public Stream<Position> getAllAvailablePositions(Device device, Date minDate,
            int maxCount) {
        Date lastAvailPos = device.getLastAvailablePositionDate(new Date());
        if(minDate == null || minDate.before(lastAvailPos))
            minDate = lastAvailPos;
        if(!user.getAdmin() 
           && !user.getAllAvailableDevices().stream()
                .anyMatch(d -> d.equals(device)))
            return Stream.empty();
        
        positionListQuery.setParameter("device", device);
        positionListQuery.setParameter("minDate", minDate);
        positionListQuery.setParameter("valid", Position.VALID_STATUS_CORRECT_POSITION);
        positionListQuery.setMaxResults(maxCount);            
        
        return positionListQuery.getResultList().stream();
    }
    
    private boolean isVisible(Position p) {
        if(user.getAdmin())
            return true;
        if(!user.getAllAvailableDevices().stream()
                .anyMatch(d -> d.equals(p.getDevice())))
            return false;
        
        Date lastAvailPos = p.getDevice().getLastAvailablePositionDate(new Date());
        return lastAvailPos.before(p.getTime());
    }
}
