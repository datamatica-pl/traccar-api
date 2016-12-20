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

import javax.persistence.EntityManager;
import javax.persistence.Query;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserSession;

public class SessionProvider {
    private final EntityManager em;
    private final User user;
    
    public SessionProvider(EntityManager em, User user) {
        this.em = em;
        this.user = user;
    }
    
    public void createSession(String id, String token) {
        UserSession us = new UserSession(id, user.getId(), token);
        em.persist(us);
    }
    
    public void deleteSession(String id) {
        Query q = em.createQuery("delete UserSession where id = :id", UserSession.class);
        q.setParameter("id", id);
        q.executeUpdate();
    }
}
