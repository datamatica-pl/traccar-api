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
package pl.datamatica.traccar.api;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import pl.datamatica.traccar.api.fcm.Daemon;

public class ClearDaemon extends Daemon{
    private final long AUDIT_REMOVE_INTERVAL = 
            TimeUnit.MILLISECONDS.convert(29, TimeUnit.DAYS);
    private static final int CHECK_HOUR = 1;
    
    @Override
    public void start(ScheduledExecutorService scheduler) {
        long mDiff = Helper.minutesToHourOfDay(CHECK_HOUR);
        start(scheduler, mDiff, 60*24);
    }

    @Override
    protected void doWork(EntityManager em) {
        clearAuditLog(em);
    }
    
    private void clearAuditLog(EntityManager em) {
        Date limitDate = new Date(System.currentTimeMillis() - AUDIT_REMOVE_INTERVAL);
        em.createQuery("delete from AuditLog al where al.time < :limitDate")
                .setParameter("limitDate", limitDate)
                .executeUpdate();
    }
    
}
