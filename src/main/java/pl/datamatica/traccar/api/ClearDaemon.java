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
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.providers.UserProvider;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

public class ClearDaemon extends Daemon{
    private final long AUDIT_REMOVE_INTERVAL = 
            TimeUnit.MILLISECONDS.convert(29, TimeUnit.DAYS);
    private static final int CHECK_HOUR = 1;
    public static final String DEMO_USER = "testowy";
    public static final String CLEARING_USER = "admin";
    
    @Override
    public void start(ScheduledExecutorService scheduler) {
        long mDiff = Helper.minutesToHourOfDay(CHECK_HOUR);
        start(scheduler, mDiff, 60*24);
    }

    @Override
    protected void doWork(EntityManager em) {
        clearUsers(em);
        clearAuditLog(em);
    }
    
    private void clearUsers(EntityManager em) {
        long now = System.currentTimeMillis();
        UserProvider up = new UserProvider(em, null, null);
        try {
            User clearing = up.getUserByLogin(CLEARING_USER);
            if(clearing == null) {
                Logger.getLogger(ClearDaemon.class.getName())
                        .log(Level.SEVERE, "No such user: "+CLEARING_USER);
                return;
            }
            up.authenticateUser(clearing.getId());
        } catch(ProviderException e) {
            Logger.getLogger(ClearDaemon.class.getName()).log(Level.SEVERE, null, e);
            return;
        }
        List<User> users = em.createQuery("select u from User u", 
                User.class).getResultList();
        for(User u : users) {
            if(DEMO_USER.equals(u.getLogin()) 
                    || u.hasPermission(UserPermission.RESOURCE_MANAGEMENT))
                continue;
            
            if(!u.hadAnyDevice()) {
                if(u.wasBleUser())
                    u.setHadAnyDevice(true);
                else {
                    boolean hasGpsDevice = !em.createQuery(
                            "select 1 from Device d where d.owner = :u")
                            .setParameter("u", u)
                            .setMaxResults(1)
                            .getResultList().isEmpty();
                    u.setHadAnyDevice(hasGpsDevice);
                }
                
                if(!u.hadAnyDevice() && getDaysCount(now, u.getRegistrationTime().getTime()) >= 7) {
                    try {
                        up.forceRemoveUser(u);
                        continue;
                    } catch (Exception ex) {
                        Logger.getLogger(ClearDaemon.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            
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
    
    private void clearAuditLog(EntityManager em) {
        Date limitDate = new Date(System.currentTimeMillis() - AUDIT_REMOVE_INTERVAL);
        em.createQuery("delete from AuditLog al where al.time < :limitDate")
                .setParameter("limitDate", limitDate)
                .executeUpdate();
    }
    
}
