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

import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.api.dtos.in.EditApplicationSettingsDto;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.AuditLog;
import pl.datamatica.traccar.model.AuditLogType;
import pl.datamatica.traccar.model.PasswordHashMethod;
import pl.datamatica.traccar.model.RulesVersion;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserGroup;
import pl.datamatica.traccar.model.UserPermission;

public class ApplicationSettingsProvider{
    private final EntityManager em;
    private UserGroupProvider userGroupsProvider;
    private User requestUser;
    private ApplicationSettings settings;

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
        if(settings == null) {
            TypedQuery<ApplicationSettings> tq = em.createQuery("Select x from ApplicationSettings x", 
                    ApplicationSettings.class);
            tq.setMaxResults(1);
            List<ApplicationSettings> result = tq.getResultList();
            settings = result.isEmpty() ? new ApplicationSettings() : result.get(0);
        }
        return settings;
    }
    
    public void updateApplicationSetting(EditApplicationSettingsDto dto) throws ProviderException {
        if (!requestUser.hasPermission(UserPermission.SERVER_MANAGEMENT))
            throw new ProviderException(ProviderException.Type.ACCESS_DENIED);
        
        ApplicationSettings as = get();
        generateAuditLogForSettingsChanged(as, dto);
        
        as.setRegistrationEnabled(dto.isRegistrationEnabled());
        as.setUpdateInterval(dto.getUpdateInterval());
        as.setDefaultHashImplementation(PasswordHashMethod.fromString(dto.getDefaultPasswordHash()));
        as.setEventRecordingEnabled(dto.isEventRecordingEnabled());
        as.setNotificationExpirationPeriod(dto.getNotificationExpirationPeriod());
        as.setLanguage(dto.getLanguage());
        as.setBingMapsKey(dto.getBingMapsKey());
        as.setMatchServiceURL(dto.getMatchServiceURL());
        
        if (requestUser.hasPermission(UserPermission.USER_GROUP_MANAGEMENT) && dto.getDefaultUserGroupId() != null) {
            UserGroup group = userGroupsProvider.getGroup(dto.getDefaultUserGroupId());
            if (!Objects.equals(as.getDefaultGroup().getId(), group.getId()))
                addSingleChangeAuditLog("defaultUserGroup", group.getId() + "");
            as.setDefaultGroup(group);
        }
        
        em.persist(as);
    }
    
    // AuditLog methods
    
    private void generateAuditLogForSettingsChanged(ApplicationSettings as, EditApplicationSettingsDto dto) {
        if (as.getRegistrationEnabled() != dto.isRegistrationEnabled())
            addSingleChangeAuditLog("registrationEnabled", dto.isRegistrationEnabled() ? "true" : "false");
        if (!Objects.equals(as.getUpdateInterval(), dto.getUpdateInterval()))
            addSingleChangeAuditLog("updateInterval", dto.getUpdateInterval().toString());
        if (!Objects.equals(as.getDefaultHashImplementation(), PasswordHashMethod.fromString(dto.getDefaultPasswordHash())))
            addSingleChangeAuditLog("defaultPasswordHash", dto.getDefaultPasswordHash());
        if (as.isEventRecordingEnabled() != dto.isEventRecordingEnabled())
            addSingleChangeAuditLog("eventRecordingEnabled", dto.isEventRecordingEnabled() ? "true" : "false");
        if (as.getNotificationExpirationPeriod() != dto.getNotificationExpirationPeriod())
            addSingleChangeAuditLog("notificationExpirationPeriod", dto.getNotificationExpirationPeriod() + "");
        if (!Objects.equals(as.getLanguage(), dto.getLanguage()))
            addSingleChangeAuditLog("language", dto.getLanguage());
        if (!Objects.equals(as.getBingMapsKey(), dto.getBingMapsKey()))
            addSingleChangeAuditLog("bingMapsKey", dto.getBingMapsKey());
        if (!Objects.equals(as.getMatchServiceURL(), dto.getMatchServiceURL()))
            addSingleChangeAuditLog("matchServiceURL", dto.getMatchServiceURL());
    }
    
    private void addSingleChangeAuditLog(String fieldName, String fieldNewValue) {
        AuditLog al = new AuditLog.Builder()
                .agentLogin(requestUser.getLogin())
                .type(AuditLogType.CHANGED_SERVER_SETTING)
                .fieldName(fieldName)
                .fieldNewValue(fieldNewValue)
                .build();
        
        em.persist(al);
    }
}
