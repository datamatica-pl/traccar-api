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

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.api.dtos.in.EditApplicationSettingsDto;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.PasswordHashMethod;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserGroup;
import pl.datamatica.traccar.model.UserPermission;

public class ApplicationSettingsProvider{
    private final EntityManager em;
    private UserGroupProvider userGroupsProvider;
    private User requestUser;

    public ApplicationSettingsProvider(EntityManager em) {
        this.em = em;
    }
    
    public void setUserGroupsProvider(UserGroupProvider ugp) {
        this.userGroupsProvider = ugp;
    }
    
    public void setRequestUser(User user) {
        this.requestUser = user;
    }
    
    public ApplicationSettings get() {
        TypedQuery<ApplicationSettings> tq = em.createQuery("Select x from ApplicationSettings x", 
                ApplicationSettings.class);
        tq.setMaxResults(1);
        List<ApplicationSettings> result = tq.getResultList();
        return result.isEmpty() ? new ApplicationSettings() : tq.getSingleResult();
    }
    
    public void updateApplicationSetting(EditApplicationSettingsDto dto) throws ProviderException {
        if (!requestUser.hasPermission(UserPermission.SERVER_MANAGEMENT))
            throw new ProviderException(ProviderException.Type.ACCESS_DENIED);
        
        ApplicationSettings as = get();

        as.setRegistrationEnabled(dto.isRegistrationEnabled());
        as.setUpdateInterval(dto.getUpdateInterval());
        as.setDefaultHashImplementation(PasswordHashMethod.fromString(dto.getDefaultPasswordHash()));
        as.setDisallowDeviceManagementByUsers(dto.isDisallowDeviceManagementByUsers());
        as.setEventRecordingEnabled(dto.isEvantRecordingEnabled());
        as.setNotificationExpirationPeriod(dto.getNotificationExpirationPeriod());
        as.setLanguage(dto.getLanguage());
        as.setBingMapsKey(dto.getBingMapsKey());
        as.setMatchServiceURL(dto.getMatchServiceURL());
        as.setAllowCommandsOnlyForAdmins(dto.isAllowCommandsOnlyForAdmins());
        
        if (requestUser.hasPermission(UserPermission.GROUP_MANAGEMENT) && dto.getDefaultUserGroupId() != null) {
            UserGroup group = userGroupsProvider.getGroup(dto.getDefaultUserGroupId());
            as.setDefaultGroup(group);
        }
        
        em.persist(as);
    }
}
