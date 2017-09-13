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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.utils.GeoUtils;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

public class PositionProvider extends ProviderBase {
    
    private final User user;
    private final TypedQuery<Position> positionListQuery;
    
    public PositionProvider(EntityManager em, User user) {
        super(em);
        this.user = user;
        
        positionListQuery = em.createQuery("from Position p "
                + "where p.device = :device and p.serverTime >= :minDate and p.serverTime <= :maxDate "
                +   "and (validStatus is null or validStatus = :valid) "
                + "order by p.serverTime", Position.class);
    }
    
    public Position get(long id) throws ProviderException {
        return get(Position.class, id, this::isVisible);
    }
    
    // If maxCount == 0, then there is no upper limit for number of positions
    public Stream<Position> getAllAvailablePositions(Device device, Date minDate,
            Date maxDate, int maxCount) throws ProviderException {
        if (!user.hasPermission(UserPermission.HISTORY_READ))
            throw new ProviderException(ProviderException.Type.ACCESS_DENIED);
        
        Date lastAvailPos = device.getLastAvailablePositionDate(new Date());
        
        if(minDate == null && user.hasPermission(UserPermission.ALL_HISTORY))
            minDate = new Date(0);
        else if (!user.hasPermission(UserPermission.ALL_HISTORY) && 
                (minDate == null || minDate.before(lastAvailPos)))
            minDate = lastAvailPos;
        
        if(maxDate == null || maxDate.after(new Date()))
            maxDate = new Date();
        if(!user.hasPermission(UserPermission.ALL_DEVICES)
           && !user.getAllAvailableDevices().stream()
                .anyMatch(d -> d.equals(device)))
            return Stream.empty();

        positionListQuery.setParameter("device", device);
        positionListQuery.setParameter("minDate", minDate);
        positionListQuery.setParameter("maxDate", maxDate);
        positionListQuery.setParameter("valid", Position.VALID_STATUS_CORRECT_POSITION);
        if(maxCount != 0)
            positionListQuery.setMaxResults(maxCount);            
        
        return positionListQuery.getResultList().stream();
    }
    
    private boolean isVisible(Position p) {
        if(user.hasPermission(UserPermission.ALL_DEVICES))
            return true;
        if(!user.hasPermission(UserPermission.HISTORY_READ))
            return false;

        if(!user.getAllAvailableDevices().stream()
                .anyMatch(d -> d.equals(p.getDevice())))
            return false;
        
        Date lastAvailPos = p.getDevice().getLastAvailablePositionDate(new Date());
        return lastAvailPos.before(p.getTime());
    }
    
    public static List<Position> filterPositions(List<Position> pos, PositionsQueryParams qp) {
        Stream<Position> filtered = pos.stream();
             
        if (qp.hideZero)
            filtered = filtered.filter(p -> p.getLatitude() != 0 || p.getLongitude() != 0);
        if (qp.hideInvalid)
            filtered = filtered.filter(p -> p.getValid() != null && p.getValid());
        if (qp.speedValue != null) {
            filtered = filtered.filter(p -> p.getSpeed() != null);
            switch (qp.speedComp) {
                case LESS: filtered = filtered.filter(p -> p.getSpeed() < qp.speedValue); break;
                case LESSEQUAL: filtered = filtered.filter(p -> p.getSpeed() <= qp.speedValue); break;
                case EQUAL: filtered = filtered.filter(p -> p.getSpeed() == qp.speedValue.doubleValue()); break;
                case GREATEREQUAL: filtered = filtered.filter(p -> p.getSpeed() >= qp.speedValue); break;
                case GREATER: filtered = filtered.filter(p -> p.getSpeed() > qp.speedValue); break;
            }
        }
        
        List<Position> posList = filtered.collect(Collectors.toList());
        if (posList.isEmpty())
            return posList;
        
        List<Position> resList = new ArrayList<>();
        Position last = posList.get(0);
        resList.add(last);
        for (int i = 1; i < posList.size(); i++) {
            boolean add = true;
            Position current = posList.get(i);
            
            if (qp.hideDuplicates && last.getTime().equals(current.getTime()))
                add = false;
            if (qp.minDistance > 0)
                if (GeoUtils.getDistance(last.getLongitude(), last.getLatitude(), current.getLongitude(), current.getLatitude()) * 1000.0 < qp.minDistance)
                    add = false;
            
            if (add) {
                resList.add(current);
                last = current;
            }
        }
        return resList;
    }
    
    public enum PositionSpeedOperator {
        LESS, LESSEQUAL, EQUAL, GREATEREQUAL, GREATER
    }
    
    public static class PositionsQueryParams {
        public Date minDate = null;
        public Date maxDate = null;
        public Boolean hideZero = false;
        public Boolean hideInvalid = false;
        public Boolean hideDuplicates = false;
        public Integer minDistance = 0;
        public PositionSpeedOperator speedComp;
        public Integer speedValue = null;
        public Boolean getAll = false;
        
        public List<ErrorDto> errors = new ArrayList<>();
        
        public PositionsQueryParams() {}
    }
}
