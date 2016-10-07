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
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.User;

public class PositionProvider extends ProviderBase {
    
    private final User user;
    
    public PositionProvider(EntityManager em, User user) {
        super(em);
        this.user = user;
    }
    
    public Position get(long id) throws ProviderException {
        return get(Position.class, id, this::isVisible);
    }
    
    public Stream<Position> getAllAvailablePositions(Device device) {
        if(user.getAdmin())
            return device.getPositions().stream();
        else
            return device.getPositions().stream()
                    .filter(this::isVisible);
    }
    
    private boolean isVisible(Position p) {
        if(user.getAdmin())
            return true;
        if(!user.getAllAvailableDevices().stream()
                .anyMatch(d -> d.equals(p.getDevice())))
            return false;
        if(!p.getDevice().isValid(new Date()))
            return false;
        int historyLength = p.getDevice().getHistoryLength();
        ZonedDateTime positionDate = p.getTime().toInstant().atZone(ZoneId.systemDefault());
        long daysDiff = ChronoUnit.DAYS.between(positionDate, ZonedDateTime.now());
        return daysDiff <= historyLength;
    }
}
