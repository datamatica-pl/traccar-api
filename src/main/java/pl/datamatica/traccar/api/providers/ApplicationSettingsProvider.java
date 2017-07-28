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

public class ApplicationSettingsProvider{
    private final EntityManager em;
    
    public ApplicationSettingsProvider(EntityManager em) {
        this.em = em;
    }
    
    public ApplicationSettings get() {
        TypedQuery<ApplicationSettings> tq = em.createQuery("Select x from ApplicationSettings x", 
                ApplicationSettings.class);
        tq.setMaxResults(1);
        List<ApplicationSettings> result = tq.getResultList();
        return result.isEmpty() ? new ApplicationSettings() : tq.getSingleResult();
    }
    
    public void updateApplicationSetting(EditApplicationSettingsDto updated) {
        ApplicationSettings as = get();
        
        as.setRegistrationEnabled(updated.isRegistrationEnabled());
        as.setUpdateInterval(updated.getUpdateInterval());
        as.setDefaultHashImplementation(PasswordHashMethod.fromString(updated.getDefaultPasswordHash()));
        as.setDisallowDeviceManagementByUsers(updated.isDisallowDeviceManagementByUsers());
        as.setEventRecordingEnabled(updated.isEvantRecordingEnabled());
        as.setNotificationExpirationPeriod(updated.getNotificationExpirationPeriod());
        as.setLanguage(updated.getLanguage());
        as.setBingMapsKey(updated.getBingMapsKey());
        as.setMatchServiceURL(updated.getMatchServiceURL());
        as.setAllowCommandsOnlyForAdmins(updated.isAllowCommandsOnlyForAdmins());
        
        em.persist(as);
    }
}
