/*
 *  Copyright (C) 2017 Datamatica (dev@datamatica.pl)
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

import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import pl.datamatica.traccar.model.AuditLog;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author piotrkrzeszewski
 */
public class AuditLogProvider extends ProviderBase {
    private final User requestUser;
    
    private long WEEK_TIME = 1000 * 60 * 60 * 24 * 7;
    
    public AuditLogProvider(EntityManager em, User requestUser) {
        super(em);
        this.requestUser = requestUser;
    }
    
    public AuditLog get(long id) throws ProviderException {
        checkAuditAccessPermission();
        
        return get(AuditLog.class, id, ag -> true);
    }
    
    public List<AuditLog> get(Date fromDate, Date toDate) throws ProviderException {
        checkAuditAccessPermission();
        
        if (fromDate == null) {
            fromDate = new Date();
            fromDate.setTime(new Date().getTime() - WEEK_TIME);
        }
        if (toDate == null)
            toDate = new Date();
        
        Query query = em.createQuery("SELECT ag FROM AuditLog WHERE ag.time >= :minDate AND ag.time <= :maxDate ORDER BY time");
        query.setParameter("minDate", fromDate);
        query.setParameter("maxDate", toDate);
        List<AuditLog> result = query.getResultList();
        
        return result;
    }
    
    private void checkAuditAccessPermission() throws ProviderException {
        if (!requestUser.hasPermission(UserPermission.AUDIT_ACCESS))
            throw new ProviderException(ProviderException.Type.ACCESS_DENIED);
    }
}
