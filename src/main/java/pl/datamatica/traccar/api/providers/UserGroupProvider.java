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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import pl.datamatica.traccar.api.dtos.in.AddUserGroupDto;
import pl.datamatica.traccar.model.AuditLog;
import pl.datamatica.traccar.model.AuditLogType;
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
    private ApplicationSettingsProvider applicationSettingsProvider;
    
    public UserGroupProvider(EntityManager em, User requestUser) {
        super(em);
        this.requestUser = requestUser;
    }
    
    public void setUserProvider(UserProvider up) {
        this.userProvider = up;
    }
    
    public void setApplicationSettingsProvider(ApplicationSettingsProvider asp) {
        this.applicationSettingsProvider = asp;
    }
    
    public UserGroup getGroup(long id) throws ProviderException {
        return get(UserGroup.class, id, this::isVisible);
    }
    
    public Stream<UserGroup> getAllAvailableGroups() throws ProviderException {
        if (!requestUser.hasPermission(UserPermission.USER_GROUP_MANAGEMENT)) {
            return Stream.of(requestUser.getUserGroup());
        }
        
        Stream<UserGroup> stream = em.createQuery("SELECT g FROM UserGroup g").getResultList().stream();
        return stream;
    }
    
    public Stream<Long> getAllGroupUsers(long id) throws ProviderException {
        //throws exception if user doesn't have access to given group
        UserGroup group = getGroup(id);
        
        return userProvider.getAllAvailableUsers().filter(u -> u.getUserGroup() != null && u.getUserGroup().equals(group)).map(u -> u.getId());
    }
    
    public void updateGroupUsers(final long id, final Set<Long> uids) throws ProviderException {
        checkGroupManagementPermission();
        final UserGroup group = getGroup(id);
        final UserGroup defaultGroup = applicationSettingsProvider.get().getDefaultGroup();
        
        userProvider.getAllManagedUsers().forEach(u -> {
            if(uids.contains(u.getId()))
                u.setUserGroup(group);
            else if(u.getUserGroup().getId() == id)
                u.setUserGroup(defaultGroup);
        });       
    }
    
    public UserGroup createUserGroup(AddUserGroupDto dto) throws ProviderException {
        checkGroupManagementPermission();
        checkNameUniqueness(dto.getName(), null);
        
        UserGroup group = new UserGroup();
        editGroupWithDto(group, dto);
        
        em.persist(group);
        addAuditLogCreateUserGroup(group);
        return group;
    }
    
    public void updateUserGroup(long id, AddUserGroupDto dto) throws ProviderException {
        checkGroupManagementPermission();
        
        UserGroup group = getGroup(id);
        checkNameUniqueness(dto.getName(), group);
        editGroupWithDto(group, dto);
        
        em.persist(group);
    }
    
    public void deleteUserGroup(long id) throws ProviderException {
        checkGroupManagementPermission();
        
        // throws if usergroup doesn't exist
        UserGroup group = getGroup(id);
        
        UserGroup defaultGroup = applicationSettingsProvider.get().getDefaultGroup();
        if (group.getId() == defaultGroup.getId())
            throw new ProviderException(ProviderException.Type.DELETING_DEFAULT);
        
        group.setPermissions(Collections.EMPTY_SET);
        em.persist(group);
        
        List<User> users = userProvider.getAllAvailableUsers().filter(u -> u.getUserGroup() != null && u.getUserGroup().equals(group)).collect(Collectors.toList());
        users.forEach((user) -> {
            user.setUserGroup(defaultGroup);
        });
        em.flush();
        
        Query query = em.createQuery("DELETE FROM UserGroup WHERE id = ?");
        query.setParameter(1, id);
        query.executeUpdate();
    }
    
    private void editGroupWithDto(UserGroup group, AddUserGroupDto dto) {
        group.setName(dto.getName());
        if (dto.getPermissions() != null)
            group.setPermissions(dto.getPermissions());
        else
            group.setPermissions(Collections.EMPTY_SET);
    }
    
    private boolean isVisible(UserGroup g) {
        return requestUser.getUserGroup().equals(g) || requestUser.hasPermission(UserPermission.USER_GROUP_MANAGEMENT);
    }
    
    private void checkNameUniqueness(String name, UserGroup group) throws ProviderException {
        List<UserGroup> groups = getAllAvailableGroups()
                .filter(g -> g.getName().toLowerCase().equals(name.toLowerCase()))
                .filter(g -> group == null || g.getId() != group.getId())
                .collect(Collectors.toList());
        if (!groups.isEmpty())
            throw new ProviderException(ProviderException.Type.GROUP_ALREADY_EXISTS);
    }
    
    private void checkGroupManagementPermission() throws ProviderException {
        if (!requestUser.hasPermission(UserPermission.USER_GROUP_MANAGEMENT)) 
            throw new ProviderException(ProviderException.Type.ACCESS_DENIED);
    }
    
    private void addAuditLogCreateUserGroup(UserGroup group) {
        AuditLog al = new AuditLog.Builder()
                        .type(AuditLogType.CREATED_USERGROUP)
                        .agentLogin(requestUser.getLogin())
                        .targetUserGroupName(group.getName())
                        .build();
        
        em.persist(al);
    }
}
