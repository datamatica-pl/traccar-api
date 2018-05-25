/*
 *  Copyright (C) 2018  Datamatica (dev@datamatica.pl)
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
package pl.datamatica.traccar.api;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.persistence.EntityManager;
import pl.datamatica.traccar.api.fcm.Daemon;
import pl.datamatica.traccar.api.providers.MailSender;
import pl.datamatica.traccar.model.UserEvent;

public class NotificationDaemon extends Daemon {
    
    @Override
    protected void doWork(EntityManager em) {
        MailSender ms = new MailSender(em);
        String appName = Application.getAppInfo().get("appName");
        String title = appName+"- %d dni nieaktywności";
        String message = "Witaj,<br/><br/>" +
                "Odnotowaliśmy, że twoje konto w systemie " + appName + " jest nieaktywne od %d dni.<br/>" +
                "Przypominamy, że konto może zostać usunięte po 180 dniach nieaktywności.<br/><br/><br/>" +
                "Dziękujemy,<br/><br/>" +
                "Zespół serwisu " + appName + "<br/><br/>" +
                "Ten email został wygenerowany automatycznie - nie odpowiadaj na niego.";
        
        for(UserEvent ue : em.createQuery(
            "SELECT e FROM "+UserEvent.class.getName()+" e INNER JOIN FETCH e.user" +
                    " WHERE e.notificationSent = :false", UserEvent.class)
                            .setParameter("false", false)
                            .getResultList()) {
            int inactive = getInactiveDaysCount(ue.getKind());
            String title1 = String.format(title, inactive);
            String message1 = String.format(message, inactive);
            if(ms.sendMessage(ue.getUser().getEmail(), title1, message1))
                ue.setNotificationSent(true);
        }
    }

    @Override
    public void start(ScheduledExecutorService scheduler) {
        start(scheduler, 0, 8*60);
    }

    private int getInactiveDaysCount(UserEvent.Type kind) {
        switch(kind) {
            case INACTIVE_30D:
                return 30;
            case INACTIVE_90D:
                return 90;
            case INACTIVE_173D:
                return 173;
        }
        return 0;
    }
}

