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

import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.model.User;

public class UserProvider {
    private final EntityManager em;
    private User requestUser;
    
    public UserProvider(EntityManager entityManager) {
        this.em = entityManager;
    }
    
    public void setRequestUser(User requestUser) {
        this.requestUser = requestUser;
    }
    
    public Stream<User> getAllAvailableUsers() {
        if(requestUser.getAdmin()) 
            return getAllUsers();
        return getAllUsers().filter(u -> isVisible(u));
    }
    
    private Stream<User> getAllUsers() {
        TypedQuery<User> tq = em.createQuery("Select x from User x", User.class);
        return tq.getResultList().stream();
    }
    
    public User getUser(long id) throws ProviderException {
        User user = em.find(User.class, id);
        if(user == null)
            throw new ProviderException(ProviderException.Type.NOT_FOUND);
        if(!isVisible(user))
            throw new ProviderException(ProviderException.Type.ACCESS_DENIED);
        return user;
    }
    
    public User getUserByMail(String email) {
        TypedQuery<User> tq = em.createQuery("Select x from User x where x.email = :email", User.class);
        tq.setParameter("email", email);
        return tq.getSingleResult();
    }
    
    public Stream<User> managedAndMe(User user) {
        if(!user.getManager())
            return Stream.of(user);
        return Stream.concat(Stream.of(user), 
                user.getManagedUsers().stream().flatMap(u -> managedAndMe(u)));
    }
    
    private boolean isVisible(User other) {
        if(requestUser.getAdmin())
            return true;
        if(requestUser.getManagedBy().equals(other))
            return true;
        
        return managedAndMe(requestUser).anyMatch(u -> u.equals(other));
    }
}
