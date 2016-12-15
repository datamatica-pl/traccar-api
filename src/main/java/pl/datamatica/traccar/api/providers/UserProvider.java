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

import java.util.UUID;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.api.auth.AuthenticationException;
import pl.datamatica.traccar.api.auth.AuthenticationException.ErrorType;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserSettings;

public class UserProvider extends ProviderBase {
    private User requestUser;
    private ApplicationSettings appSettings;
    
    public UserProvider(EntityManager entityManager, ApplicationSettings appSettings) {
        super(entityManager);
        this.appSettings = appSettings;
    }
    
    public User authenticateUser(String email, String password) throws AuthenticationException {
        if(email == null || email.isEmpty())
            throw new AuthenticationException(ErrorType.NO_SUCH_USER);
        if(password == null || password.isEmpty())
            throw new AuthenticationException(ErrorType.NO_PASSWORD);
        
        User user = getUserByLogin(email);
        if(user == null)
            throw new AuthenticationException(ErrorType.NO_SUCH_USER);
        if(user.getPasswordHashMethod().doHash(password, appSettings.getSalt()).equals(user.getPassword())) {
            requestUser = user;
            return user;
        }
        throw new AuthenticationException(ErrorType.NO_SUCH_USER);
    }
    
    public User authenticateUser(long id) throws ProviderException {
        return get(User.class, id, u -> true);
    }
    
    public User getRequestUser() {
        return requestUser;
    }
    
    public Stream<User> getAllAvailableUsers() {
        if(requestUser.getAdmin()) 
            return getAllUsers();
        return Stream.concat(requestUser.getAllManagedUsers().stream(), 
                Stream.of(requestUser.getManagedBy(), requestUser));
    }
    
    public User getUser(long id) throws ProviderException {
        return get(User.class, id, this::isVisible);
    }

    public User createUser(String email, String password, boolean checkMarketing) 
            throws ProviderException {
        User existing = getUserByLogin(email);
        if(existing != null)
            throw new ProviderException(Type.USER_ALREADY_EXISTS);
        
        String hashedPassword = appSettings.getDefaultHashImplementation().doHash(password, appSettings.getSalt());
        User user = new User(email, hashedPassword);
        user.setEmail(email);
        user.setManager(true);
        user.setMarketingCheck(checkMarketing);
        user.setEmailValid(false);
        user.setBlocked(true);
        user.setPasswordHashMethod(appSettings.getDefaultHashImplementation());
        user.setUserSettings(new UserSettings());
        user.setEmailValidationToken(generateEmailValidationToken());
        em.persist(user);
        
        return user;
    }

    private String generateEmailValidationToken() {
        TypedQuery<Long> tq = em.createQuery("select x.id from User x where x.emailValidationToken = :token", Long.class);
        tq.setMaxResults(1);
        while(true) {
            UUID token = UUID.randomUUID();
            tq.setParameter("token", token.toString());
            if(tq.getResultList().isEmpty())
                return token.toString();
        }
    }
    
    public void activateUser(String token) throws ProviderException {
        User user = getUserByToken(token);
        if(user == null) 
            throw new ProviderException(Type.NOT_FOUND);
        user.setBlocked(false);
        user.setEmailValid(true);
        user.setEmailValidationToken(null);
        em.persist(user);
    }
    
    private User getUserByLogin(String login) {
        try {
            TypedQuery<User> tq = em.createQuery("Select x from User x where x.login = :login", User.class);
            tq.setParameter("login", login);
            return tq.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    private User getUserByToken(String token) {
        try {
            TypedQuery<User> tq = em.createQuery("Select x from User x where x.emailValidationToken = :token", User.class);
            tq.setParameter("token", token);
            return tq.getSingleResult();
        } catch(NoResultException e) {
            return null;
        }
    }
    
    private Stream<User> getAllUsers() {
        TypedQuery<User> tq = em.createQuery("Select x from User x", User.class);
        return tq.getResultList().stream();
    }
    
    private boolean isVisible(User other) {
        if(requestUser == null)
            return false;
        if(requestUser.getAdmin())
            return true;
        
        return getAllAvailableUsers().anyMatch(u -> u.equals(other));
    }
}
