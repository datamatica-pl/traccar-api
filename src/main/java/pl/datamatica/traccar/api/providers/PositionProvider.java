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
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.User;

public class PositionProvider extends ProviderBase {
    
    private User user;
    
    public PositionProvider(EntityManager em, User user) {
        super(em);
        this.user = user;
    }
    
    public Position get(long id) throws ProviderException {
        return get(Position.class, id, 
                p -> user.getAllAvailableDevices().stream()
                        .anyMatch(d -> d.equals(p.getDevice())));
    }
}
