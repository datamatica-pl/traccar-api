/*
 *  Copyright (C) 2016  Datamatica (dev@datamatica.pl)
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

import javax.persistence.EntityManager;
import org.slf4j.Logger;
import pl.datamatica.traccar.api.dtos.out.NotificationSettingsDto;
import pl.datamatica.traccar.model.MobNotificationMode;
import pl.datamatica.traccar.model.MobNotificationType;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

public class NotificationSettingsProvider {
    
    private final EntityManager em;
    private final User requestUser;
    private Logger logger;
    
    public NotificationSettingsProvider(EntityManager em, User requestUser) {
        this.em = em;
        this.requestUser = requestUser;
        logger = DbLog.getLogger();
    }
    
    public void updateNotificationSettings(NotificationSettingsDto dto) throws ProviderException {
        if (!requestUser.hasPermission(UserPermission.NOTIFICATIONS))
            throw new ProviderException(ProviderException.Type.ACCESS_DENIED);
        
        updateNotificationSetting(MobNotificationType.GEOFENCE, dto.getGeofence());
        updateNotificationSetting(MobNotificationType.OVERSPEED, dto.getOverspeed());
        updateNotificationSetting(MobNotificationType.POLICY, dto.getPolicy());
        updateNotificationSetting(MobNotificationType.MAINTENANCE, dto.getMaintenance());
        em.persist(requestUser);
        logger.info("{} has changed his notification settings", requestUser.getLogin());
    }
    
    private void updateNotificationSetting(MobNotificationType t, String mode) {
        if(mode.equals("NONE"))
            requestUser.getMobileNotificationSettings().remove(t);
        else {
            MobNotificationMode m = MobNotificationMode.valueOf(mode);
            requestUser.getMobileNotificationSettings().put(t, m);
        }
    }
}
