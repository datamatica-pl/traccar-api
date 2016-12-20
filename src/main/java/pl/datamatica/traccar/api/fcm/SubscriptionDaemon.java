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
package pl.datamatica.traccar.api.fcm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserSession;

public class SubscriptionDaemon extends Daemon {    
    @Override
    public void start(ScheduledExecutorService scheduler) {
        Calendar calendar = Calendar.getInstance();
        if(calendar.get(Calendar.HOUR_OF_DAY) > 10)
            calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        long mDiff = (calendar.getTimeInMillis()-System.currentTimeMillis())/(60*1000);
        start(scheduler, mDiff, 60*24);
    }

    @Override
    protected void doWork(EntityManager em) {
        Calendar calendar = Calendar.getInstance();
        TypedQuery<Device> tq = em.createQuery("from Device d "
                + "join fetch d.users "
                + "where d.validTo < :thresholdMax", 
                Device.class);        
        calendar.add(Calendar.DATE, 7);
        tq.setParameter("thresholdMax", calendar.getTime());
        
        Map<User, List<Device>> users = new HashMap<>();
        for(Device d : tq.getResultList())
            for(User u : d.getUsers()) {
                if(users.containsKey(u))
                    users.get(u).add(d);
                else
                    users.put(u, new ArrayList<>(Collections.singleton(d)));
            }
        
        for(Map.Entry<User, List<Device>> entry:users.entrySet())
            sendNotification(em, entry.getKey(), entry.getValue());
    }

    private void sendNotification(EntityManager em, User user, List<Device> devices) {
        for(UserSession session : user.getSessions()) {
            FcmNotificationDto dto = FcmNotificationDto.subsciption(
                    devices,
                    session.getFcmToken());
            sendToFcm(em, dto);
        }
    }
}
