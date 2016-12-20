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
package pl.datamatica.traccar.api.fcm;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.model.DeviceEvent;
import pl.datamatica.traccar.model.MobNotificationMode;
import pl.datamatica.traccar.model.MobNotificationType;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserSession;

public class AlarmDaemon extends Daemon{

    @Override
    public void start(ScheduledExecutorService executor) {
        start(executor, 0, 1);
    }
    
    protected void doWork(EntityManager em) {
        Calendar calendar = Calendar.getInstance();
        TypedQuery<DeviceEvent> tq = em.createQuery("from DeviceEvent ev "
                + "inner join fetch ev.device as dev "
                + "inner join fetch dev.users as usr "
                + "where ev.time > :prev_test_time", 
                DeviceEvent.class);
        calendar.add(Calendar.MINUTE, -1);
        tq.setParameter("prev_test_time", calendar.getTime());
        
        Set<User> users = new HashSet<>();
        for(DeviceEvent ev : tq.getResultList())
            for(User u : ev.getDevice().getUsers()) {
                if(u.acceptsNotification(ev.getType()))
                    users.add(u);
            }
        
        for(User u : users) {
            for(UserSession session : u.getSessions())
                sendNotification(em, session);
        }
    }

    private void sendNotification(EntityManager em, UserSession session) {
        FcmNotificationDto dto = FcmNotificationDto.alarms(session.getFcmToken());
        sendToFcm(em, dto);
    } 
}