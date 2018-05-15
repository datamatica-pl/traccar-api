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

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import pl.datamatica.traccar.api.fcm.Daemon;
import pl.datamatica.traccar.api.providers.UserProvider;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

public class ClearDaemon extends Daemon {
    public static final String DEMO_USER = "testowy";
    private static final int CHECK_HOUR = 1;

    @Override
    public void start(ScheduledExecutorService scheduler) {
        Calendar calendar = Calendar.getInstance();
        if(calendar.get(Calendar.HOUR_OF_DAY) >= CHECK_HOUR)
            calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, CHECK_HOUR);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long mDiff = (calendar.getTimeInMillis()-System.currentTimeMillis())/(60*1000);
        start(scheduler, mDiff, 60*24);
    }

    @Override
    protected void doWork(EntityManager em) {
        long now = System.currentTimeMillis();
        UserProvider up = new UserProvider(em, null, null);
        List<User> users = em.createQuery("select u from User u", 
                User.class).getResultList();
        for(User u : users) {
            if(DEMO_USER.equals(u.getLogin()) 
                    || u.hasPermission(UserPermission.RESOURCE_MANAGEMENT))
                continue;
            Date newestPos = null;
            Set<User> uAndManaged = new HashSet<>(u.getAllManagedUsers());
            uAndManaged.add(u);
            long inactiveDays = Long.MAX_VALUE;
            for(User u1 : uAndManaged)
                inactiveDays = Math.min(inactiveDays, 
                        getDaysCount(now, u1.getLastRequestTime().getTime()));
            newestPos = em.createQuery(
                "select max(d.latestPosition.serverTime) from Device d"
                        + " where d.owner in :users", Date.class)
                .setParameter("users", uAndManaged)
                .getSingleResult();
            if(newestPos != null) {
                inactiveDays = Math.min(inactiveDays, 
                        getDaysCount(now, newestPos.getTime()));
            }
            
            if(inactiveDays >= 180) {
                try {
                    up.forceRemoveUser(u);
                } catch (Exception ex) {
                    Logger.getLogger(ClearDaemon.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    private long getDaysCount(long to, long from) {
        return TimeUnit.DAYS.convert(to - from, TimeUnit.MILLISECONDS);
    }
    
}
