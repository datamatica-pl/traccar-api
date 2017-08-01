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

import java.util.Collections;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import pl.datamatica.traccar.api.dtos.in.AddDeviceGroupDto;
import pl.datamatica.traccar.model.Group;
import pl.datamatica.traccar.model.User;

public class DeviceGroupProvider extends ProviderBase {
    private final User requestUser;
    
    public DeviceGroupProvider(EntityManager em, User requestUser) {
        super(em);
        this.requestUser = requestUser;
    }
    
    public Group getGroup(long id) throws ProviderException {
        return get(Group.class, id, 
                g -> requestUser.getAdmin() || requestUser.getGroups().contains(g));
    }
    
    public Stream<Group> getAllAvailableGroups() throws ProviderException {
        return requestUser.getGroups().stream();
    }
    
    public void updateGroup(long id, AddDeviceGroupDto dto) throws ProviderException {
        Group group = getGroup(id);
        editGroupWithDto(group, dto);
        
        em.persist(group);
    }
    
    public Group createGroup(AddDeviceGroupDto dto) throws ProviderException {
        Group group = new Group();
        editGroupWithDto(group, dto);
        
        em.persist(group);
        return group;
    }
    
    private void editGroupWithDto(Group group, AddDeviceGroupDto dto) throws ProviderException {
        group.setDescription(dto.getDescription());
        group.setName(dto.getName());
        if (dto.getParent_id() != null)
            group.setParent(getGroup(dto.getParent_id()));
        else {
            group.setParent(null);
        }
        group.setUsers(Collections.singleton(requestUser));
        
    }
}
