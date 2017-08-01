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
import java.util.List;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.Query;
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
        if (requestUser.getAdmin()) {
            Query query = em.createQuery("SELECT g FROM Group g");
            return query.getResultList().stream();
        }
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
    
    public void deleteGroup(long id) throws ProviderException {
        Group group = getGroup(id);
        if (requestUser.getAdmin() || group.getUsers().size() == 1) {
            hardRemoveGroup(group);
        }
        else {
            group.getUsers().remove(requestUser);
        }
    }
    
    /// Method returns true if adding parentId's group as childId group's parent won't create cycle in tree
    public boolean checkCorrectnessOfGroupTree(long childId, long parentId) throws ProviderException {
        if (childId == parentId)
            return false;
        
        Long r = parentId;  
        do { 
            Group parent = get(Group.class, r, g -> true).getParent();
            if (parent == null) {
                // We reached root without cycle
                return true;
            }
            r = parent.getId();
        } while (r != childId && r != parentId);
        //We broke loop and there would be a cycle
        return false;
    }
    
    private void editGroupWithDto(Group group, AddDeviceGroupDto dto) throws ProviderException {
        group.setDescription(dto.getDescription());
        group.setName(dto.getName());
        if (dto.getParentId() != null)
            group.setParent(getGroup(dto.getParentId()));
        else {
            group.setParent(null);
        }
        group.setUsers(Collections.singleton(requestUser));
    }
    
    private void hardRemoveGroup(Group group) {
        Query query = em.createQuery("SELECT g FROM Group g WHERE parent_id = :id");
        query.setParameter("id", group.getId());
        for (Group gr : (List<Group>) query.getResultList()) {
            gr.setParent(group.getParent());
        }
        
        group.getUsers().clear();
        em.flush();
    
        query = em.createQuery("DELETE FROM Group WHERE id = ?");
        query.setParameter(1, group.getId());
        query.executeUpdate();
    }
}
