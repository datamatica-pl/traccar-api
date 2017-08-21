/*
 *  Copyright (C) 2017  Datamatica (dev@datamatica.pl)
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *  
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *  
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pl.datamatica.traccar.api.providers;


import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import pl.datamatica.traccar.model.Group;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserGroup;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author piotrkrzeszewski
 */
public class UserGroupProvider extends ProviderBase {
    private final User requestUser;
    
    private UserProvider userProvider;
    
    public UserGroupProvider(EntityManager em, User requestUser) {
        super(em);
        this.requestUser = requestUser;
    }
    
    public UserGroup getGroup(long id) throws ProviderException {
        return get(UserGroup.class, id, this::isVisible);
    }
    
    public Stream<UserGroup> getAllAvailableGroups() throws ProviderException {
        if (!requestUser.hasPermission(UserPermission.GROUP_MANAGEMENT))
            throw new ProviderException(ProviderException.Type.ACCESS_DENIED);
        
        Stream<UserGroup> stream = em.createQuery("SELECT g FROM UserGroup g").getResultList().stream();
        return stream;
    }
    
    private boolean isVisible(UserGroup g) {
        return requestUser.hasPermission(UserPermission.GROUP_MANAGEMENT);
    }
}
