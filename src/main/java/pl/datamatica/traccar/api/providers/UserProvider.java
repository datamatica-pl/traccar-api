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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import pl.datamatica.traccar.api.auth.AuthenticationException;
import pl.datamatica.traccar.api.auth.AuthenticationException.ErrorType;
import pl.datamatica.traccar.api.dtos.in.EditUserDto;
import pl.datamatica.traccar.api.dtos.out.UserDto;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.DeviceEventType;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserSettings;

public class UserProvider extends ProviderBase {
    private User requestUser;
    private ApplicationSettings appSettings;
    private Logger logger;
    
    public UserProvider(EntityManager entityManager, ApplicationSettings appSettings) {
        super(entityManager);
        this.appSettings = appSettings;
        logger = DbLog.getLogger();
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
    
    public User authenticateUser(long id) throws ProviderException, 
            AuthenticationException {
        requestUser = get(User.class, id, u -> true);
        return requestUser;
    }
    
    public User getRequestUser() {
        return requestUser;
    }
    
    public Stream<User> getAllAvailableUsers() {
        if(requestUser.getAdmin()) 
            return getAllUsers();
        return Stream.concat(requestUser.getAllManagedUsers().stream(), 
                requestUser.getManagedBy() == null ?
                        Stream.of(requestUser) :
                        Stream.of(requestUser, requestUser.getManagedBy()));
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
        user.setEmailValidationToken(generateToken("emailValidationToken"));
        em.persist(user);
        
        logger.info("{} created his account", user.getLogin());
        return user;
    }
    
    public void updateUser(long id, EditUserDto dto) throws ProviderException {
        User u = getUser(id);
        u.setAdmin(dto.isAdmin());
        u.setArchive(dto.isArchive());
        u.setBlocked(dto.isBlocked());
        u.setCompanyName(dto.getCompanyName());
        u.setEmail(dto.getEmail());
        u.setExpirationDate(dto.getExpirationDate());
        u.setFirstName(dto.getFirstName());
        u.setLastName(dto.getLastName());
        u.setManager(dto.isManager());
        u.setMaxNumOfDevices(dto.getMaxNumOfDevices());
        Set<DeviceEventType> notificationEvents = new HashSet<>();
        for(String ev : dto.getNotificationEvents()) {
            notificationEvents.add(DeviceEventType.valueOf(ev));
        }
        u.setNotificationEvents(notificationEvents);
        if(!EditUserDto.PASSWORD_PLACEHOLDER.equals(dto.getPassword()))
            u.setPassword(u.getPasswordHashMethod()
                    .doHash(dto.getPassword(), appSettings.getSalt()));
        u.setPhoneNumber(dto.getPhoneNumber());
        u.setReadOnly(dto.isReadOnly());
        em.persist(u);
        
        logger.info("{} updated user {}", requestUser.getLogin(), u.getLogin());
    }

    private String generateToken(String colName) {
        TypedQuery<Long> tq = em.createQuery("select x.id from User x "
                + "where x."+colName+" = :token", Long.class);
        tq.setMaxResults(1);
        while(true) {
            UUID token = UUID.randomUUID();
            tq.setParameter("token", token.toString());
            if(tq.getResultList().isEmpty())
                return token.toString();
        }
    }
    
    public void activateUser(String token) throws ProviderException {
        User user = getUserByToken("emailValidationToken", token);
        if(user == null) 
            throw new ProviderException(Type.NOT_FOUND);
        user.setBlocked(false);
        user.setEmailValid(true);
        user.setEmailValidationToken(null);
        em.persist(user);
        logger.info("{} activated his account", user.getLogin());
    }
    
    public String requestPasswordReset(String login) {
        User u = getUserByLogin(login);
        u.setPassResetToken(generateToken("passResetToken"));
        em.persist(u);
        return u.getPassResetToken();
    }
    
    public User resetPassword(String token) throws ProviderException {
        User user = getUserByToken("passResetToken", token);
        if(user == null)
            throw new ProviderException(Type.NOT_FOUND);
        user.setPassResetToken(null);
        StringBuilder pass = new StringBuilder();
        Random r = new Random();
        for(int i=0;i<14;++i)
            pass.appendCodePoint(r.nextInt(94)+33);
        user.setPassword(user.getPasswordHashMethod()
                .doHash(pass.toString(), appSettings.getSalt()));
        user.setPasswordRaw(pass.toString());
        em.persist(user);
        return user;
    }
    
    public User getUserByLogin(String login) {
        try {
            TypedQuery<User> tq = em.createQuery("Select x from User x where x.login = :login", User.class);
            tq.setParameter("login", login);
            return tq.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    private User getUserByToken(String tokenType, String token) {
        try {
            TypedQuery<User> tq = em.createQuery("Select x from User x "
                    + "where x."+tokenType+" = :token", User.class);
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
