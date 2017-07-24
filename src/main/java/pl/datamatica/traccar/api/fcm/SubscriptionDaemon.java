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
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserSession;

public class SubscriptionDaemon extends Daemon {
    private static final int CHECK_HOUR = 10;
    
    @Override
    public void start(ScheduledExecutorService scheduler) {
        Calendar calendar = Calendar.getInstance();
        if(calendar.get(Calendar.HOUR_OF_DAY) > CHECK_HOUR)
            calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, CHECK_HOUR);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long mDiff = (calendar.getTimeInMillis()-System.currentTimeMillis())/(60*1000);
        Logger.getLogger(SubscriptionDaemon.class.getSimpleName())
                .log(Level.INFO, "start: first check after "+mDiff+" minutes");
        start(scheduler, mDiff, 60*24);
    }

    @Override
    protected void doWork(EntityManager em) {
        Calendar calendar = Calendar.getInstance();
        TypedQuery<Device> tq = em.createQuery("from Device d "
                + "join fetch d.users "
                + "where d.validTo between :thresholdMin and :thresholdMax", 
                Device.class);        
        calendar.add(Calendar.DATE, 7);
        tq.setParameter("thresholdMax", calendar.getTime());
        calendar.add(Calendar.DATE, -1);
        tq.setParameter("thresholdMin", calendar.getTime());
        
        Map<User, List<Device>> users = new HashMap<>();
        for(Device d : tq.getResultList())
            for(User u : d.getUsers()) {
                if(users.containsKey(u))
                    users.get(u).add(d);
                else
                    users.put(u, new ArrayList<>(Collections.singleton(d)));
            }
        
        for(Map.Entry<User, List<Device>> entry:users.entrySet())
            if(!entry.getKey().isBlocked())
                sendNotification(em, entry.getKey(), entry.getValue());
    }

    private void sendNotification(EntityManager em, User user, List<Device> devices) {
        clearInactiveSessions(em, user);
        for(UserSession session : user.getSessions()) {
            FcmNotificationDto dto = FcmNotificationDto.subsciption(
                    devices,
                    session.getFcmToken());
            sendToFcm(em, dto);
        }
    }
}
