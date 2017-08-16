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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.api.dtos.in.AddDeviceGroupDto;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Group;
import pl.datamatica.traccar.model.User;

public class DeviceGroupProvider extends ProviderBase {
    private final User requestUser;
    
    private DeviceProvider devicesProvider;
    
    public DeviceGroupProvider(EntityManager em, User requestUser) {
        super(em);
        this.requestUser = requestUser;
    }
    
    public void setDeviceProvider(DeviceProvider devices) {
        devicesProvider = devices;
    }
    
    public Group getEditableGroup(long id) throws ProviderException {
        return get(Group.class, id, this::isVisible);
    }
    
    public Group getGroup(long id) throws ProviderException {
        Group g = new Group(get(Group.class, id, this::isVisible));
        if(!requestUser.getAdmin()) {
            Set<User> u = new HashSet<>(requestUser.getAllManagedUsers());
            u.add(requestUser);
            g.getUsers().retainAll(u);
        }
        return g;
    }
    
    public Group getSingleGroup(long id)  throws ProviderException {
        List<Group> allAvailable = getAllAvailableGroups().collect(Collectors.toList());
        
        List<Group> group = allAvailable.stream().filter(g -> g.getId() == id).collect(Collectors.toList());
        if (group.size() == 1) {
            return group.get(0);
        }
        
        //This method should throw exception, otherwise proper group would be found earlier.
        get(Group.class, id, g -> true);
        
        throw new ProviderException(ProviderException.Type.ACCESS_DENIED);
    }
    
    public Stream<Group> getAllAvailableGroups() throws ProviderException {
        Set<Group> groups = devicesProvider.getAllAvailableDevices()
                .filter(d -> d.getGroup() != null)
                .map(d -> d.getGroup())
                .map(g -> {g.setOwned(false); return g;})
                .collect(Collectors.toSet());
        
        Stream<Group> stream;
        if (requestUser.getAdmin()) {
            stream = em.createQuery("SELECT g FROM Group g").getResultList().stream();
        }
        else {
            stream = requestUser.getGroups().stream();
        }
        Set<Group> result = stream.map(g -> {g.setOwned(true); return g;}).collect(Collectors.toSet());
        result.addAll(groups);
        return result.stream();
    }
          
    public Group createGroup(AddDeviceGroupDto dto) throws ProviderException {
        Group group = new Group();
        editGroupWithDto(group, dto);
        group.setUsers(Collections.singleton(requestUser));
        
        em.persist(group);
        return group;
    }
    
    public void updateGroup(long id, AddDeviceGroupDto dto) throws ProviderException {
        Group group = getEditableGroup(id);
        editGroupWithDto(group, dto);
        
        em.persist(group);
    }
    
    public void deleteGroup(long id) throws ProviderException {
        Group group = getEditableGroup(id);
        
        hardRemoveGroup(group);
    }
    
    private void editGroupWithDto(Group group, AddDeviceGroupDto dto) throws ProviderException {
        group.setDescription(dto.getDescription());
        group.setName(dto.getName());
    }
    
    private void hardRemoveGroup(Group group) {
        // This feature isn't supported (parentGroup) but for consistency of foreign keys this SQL shouldn't be removed
        Query query = em.createQuery("SELECT g FROM Group g WHERE parent_id = :id");
        query.setParameter("id", group.getId());
        for (Group gr : (List<Group>) query.getResultList()) {
            gr.setParent(group.getParent());
        }
        
        query = em.createQuery("SELECT d FROM Device d WHERE d.group.id = :id");
        query.setParameter("id", group.getId());
        for (Device dev : (List<Device>) query.getResultList()) {
            dev.setGroup(group.getParent());
        }
        
        group.getUsers().clear();
        em.flush();
    
        query = em.createQuery("DELETE FROM Group WHERE id = ?");
        query.setParameter(1, group.getId());
        query.executeUpdate();
    }

    public void updateGroupShare(long id, List<Long> uids) throws ProviderException {
        Group g = get(Group.class, id, this::isVisible);
        if(requestUser.getAdmin())
            g.getUsers().clear();
        else {
            g.getUsers().removeAll(requestUser.getAllManagedUsers());
            g.getUsers().remove(requestUser);
        }
        Set<Long> ids = new HashSet<>(uids);
        List<User> users;
        if(requestUser.getAdmin()) {
            TypedQuery<User> tq = em.createQuery("from User u where u.id in :ids", User.class);
            tq.setParameter("ids", uids);
            users = tq.getResultList();
        } else {
            users = new ArrayList<>(requestUser.getManagedUsers());
            users.add(requestUser);
            users.removeIf(u -> !ids.contains(u.getId()));
        }
        g.getUsers().addAll(users);
    }
    
    private boolean isVisible(Group g) {
        return requestUser.getAdmin() || requestUser.getGroups().contains(g);
    }
}
