/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.datamatica.traccar.api.providers;

import javax.persistence.EntityManager;
import pl.datamatica.traccar.api.dtos.out.NotificationSettingsDto;
import pl.datamatica.traccar.model.MobNotificationMode;
import pl.datamatica.traccar.model.MobNotificationType;
import pl.datamatica.traccar.model.User;

public class NotificationSettingsProvider {
    
    private final EntityManager em;
    private final User requestUser;
    
    public NotificationSettingsProvider(EntityManager em, User requestUser) {
        this.em = em;
        this.requestUser = requestUser;
    }
    
    public void updateNotificationSettings(NotificationSettingsDto dto) {
        updateNotificationSetting(MobNotificationType.GEOFENCE, dto.getGeofence());
        updateNotificationSetting(MobNotificationType.OVERSPEED, dto.getOverspeed());
        updateNotificationSetting(MobNotificationType.POLICY, dto.getPolicy());
        updateNotificationSetting(MobNotificationType.MAINTENANCE, dto.getMaintenance());
        em.persist(requestUser);
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
