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

import javax.persistence.EntityManager;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.User;

public class PositionProvider {
    
    private EntityManager em;
    private DeviceProvider dp;
    
    public PositionProvider(EntityManager em) {
        this.em = em;
        dp = new DeviceProvider(em);
    }
    
    public void setRequestUser(User user) {
        dp.setRequestUser(user);
    }
    
    public Position get(long id) throws ProviderException {
        Position position = em.find(Position.class, id);
        if(position == null)
            throw new ProviderException(Type.NOT_FOUND);
        if(!dp.getAllAvailableDevices().anyMatch(d -> d.equals(position.getDevice())))
            throw new ProviderException(Type.ACCESS_DENIED);
        return position;
    }
}
