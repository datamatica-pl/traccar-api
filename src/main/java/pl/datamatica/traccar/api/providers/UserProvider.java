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
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.model.PasswordHashMethod;
import pl.datamatica.traccar.model.User;

public class UserProvider extends ProviderBase {
    private User requestUser;
    private PasswordHashMethod hashMethod;
    private String salt;
    
    public UserProvider(EntityManager entityManager) {
        super(entityManager);
    }
    
    public void setRequestUser(User requestUser) {
        this.requestUser = requestUser;
    }
    
    public void setPasswordHashMethod(PasswordHashMethod hashMethod) {
        this.hashMethod = hashMethod;
    }
    
    public void setPasswordHashSalt(String salt) {
        this.salt = salt;
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
        return get(User.class, id, this::isVisible);
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
        if(requestUser == null || requestUser.getAdmin())
            return true;
        if(requestUser.getManagedBy().equals(other))
            return true;
        
        return managedAndMe(requestUser).anyMatch(u -> u.equals(other));
    }

    public User createUser(String email, String password, String checkMarketing) throws ProviderException {
        User existing = getUserByMail(email);
        if(existing != null)
            throw new ProviderException(Type.ALREADY_EXISTS);
        
        em.getTransaction().begin();
        User user = new User(email, hashMethod.doHash(password, salt));
        user.setEmail(email);
        user.setManager(true);
        user.setMarketingCheck(true);
        em.persist(user);
        em.getTransaction().commit();
        
        return user;
    }
}
