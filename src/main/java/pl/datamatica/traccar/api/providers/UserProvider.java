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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import pl.datamatica.traccar.api.auth.AuthenticationException;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.in.AddUserDto;
import pl.datamatica.traccar.api.dtos.in.BleDeviceDto;
import pl.datamatica.traccar.api.dtos.in.EditUserDto;
import pl.datamatica.traccar.api.dtos.in.EditUserSettingsDto;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.AuditLog;
import pl.datamatica.traccar.model.AuditLogType;
import pl.datamatica.traccar.model.BleDevice;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.DeviceEventType;
import pl.datamatica.traccar.model.PositionIconType;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Group;
import pl.datamatica.traccar.model.Route;
import pl.datamatica.traccar.model.RulesVersion;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;
import pl.datamatica.traccar.model.UserSettings;
import pl.datamatica.traccar.model.UserSettings.SpeedUnit;

public class UserProvider extends ProviderBase {
    private User requestUser;
    private ApplicationSettings appSettings;
    private RulesProvider rulesProvider;
    private Logger logger;
    
    public UserProvider(EntityManager entityManager, ApplicationSettings appSettings, 
            RulesProvider rulesProvider) {
        super(entityManager);
        this.appSettings = appSettings;
        logger = DbLog.getLogger();
        this.rulesProvider = rulesProvider;
    }
    
    public User authenticateUser(String email, String password) throws AuthenticationException {
        if(email == null || email.isEmpty())
            throw new AuthenticationException(MessageKeys.ERR_AUTH_NO_SUCH_USER);
        if(password == null || password.isEmpty())
            throw new AuthenticationException(MessageKeys.ERR_AUTH_NO_PASSWORD);
        
        User user = getUserByLogin(email);
        if(user == null)
            throw new AuthenticationException(MessageKeys.ERR_AUTH_NO_SUCH_USER);
        if(user.getPasswordHashMethod().doHash(password, appSettings.getSalt()).equals(user.getPassword())) {
            requestUser = user;
            return user;
        }
        throw new AuthenticationException(MessageKeys.ERR_AUTH_NO_SUCH_USER);
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
        if(requestUser.hasPermission(UserPermission.ALL_USERS)) 
            return getAllUsers();
        return Stream.concat(requestUser.getAllManagedUsers().stream(), Stream.of(requestUser));
    }
    
    public Stream<User> getAllManagedUsers() {
        if(requestUser.hasPermission(UserPermission.ALL_USERS))
            return getAllUsers();
        return Stream.concat(requestUser.getAllManagedUsers().stream(),
                Stream.of(requestUser));
    }
    
    public User getUser(long id) throws ProviderException {
        return get(User.class, id, this::isVisible);
    }
    
    public User createUser(AddUserDto dto) throws ProviderException {
        checkUserManagementPermissions(requestUser);
        
        User user = prepareNewUser(dto.getLogin(), dto.getPassword());
        
        editUser(user, dto);
        user.setManagedBy(requestUser);
        
        em.persist(user);
        return user;
    }

    public User registerUser(String email, String password, boolean checkMarketing) 
            throws ProviderException {
        User user = prepareNewUser(email, password);
        user.setEmail(email);
        user.setManager(true);
        user.setMarketingCheck(checkMarketing);
        user.setEmailValid(false);
        user.setBlocked(true);
        user.setEmailValidationToken(generateToken("emailValidationToken"));
        em.persist(user);
        
        logger.info("{} created his account", user.getLogin());
        return user;
    }
    
    private User prepareNewUser(String login, String password) throws ProviderException {
        User existing = getUserByLogin(login);
        if(existing != null)
            throw new ProviderException(Type.USER_ALREADY_EXISTS);
        
        String hashedPassword = appSettings.getDefaultHashImplementation().doHash(password, appSettings.getSalt());
        User user = new User(login, hashedPassword);
        user.setPasswordHashMethod(appSettings.getDefaultHashImplementation());
        user.setUserSettings(new UserSettings());
        user.setUserGroup(appSettings.getDefaultGroup());
        for(RulesVersion rv : rulesProvider.getActiveRules())
            if(rv.getType() == RulesVersion.Type.MARKETING && !user.getMarketingCheck()) {
                user.addRulesRejection(rv);
            } else {
                user.addRulesAcceptance(rv);
            }
        user.setLastRequestTime(new Date());
        user.setRegistrationTime(new Date());
        
        generateAuditLogForCreateRemoveUser(user.getLogin(), false);
        return user;
    }
    
    public void updateUser(long id, EditUserDto dto) throws ProviderException {
        if (requestUser.getId() != id)
            checkUserManagementPermissions(requestUser);
        
        User u = getUser(id);

        editUser(u, dto);
        
        if(!EditUserDto.PASSWORD_PLACEHOLDER.equals(dto.getPassword())) {
            u.setPassword(u.getPasswordHashMethod()
                    .doHash(dto.getPassword(), appSettings.getSalt()));
            addSingleEditUserAuditLog(u.getLogin(), "password", null);
        }
        em.persist(u);
        
        logger.info("{} updated user {}", requestUser.getLogin(), u.getLogin());
    }
    
    // REMOVING USER - START //
    
    public void removeUser(long id) throws Exception {
        if (requestUser.getId() == id) 
            throw new ProviderRemovingException(Type.DELETING_ITSELF);
        if (requestUser.getManagedBy() != null && requestUser.getManagedBy().getId() == id) 
            throw new ProviderRemovingException(Type.ACCESS_DENIED);
        
        User user;
        try {
            checkUserManagementPermissions(requestUser);
            user = getUser(id);
        } catch (ProviderException pe) {
            if (pe.getType() == Type.ACCESS_DENIED)
                throw new ProviderRemovingException(Type.ACCESS_DENIED);
            if (pe.getType() == Type.NOT_FOUND)
                throw new ProviderRemovingException(Type.NOT_FOUND);
            throw pe;
        }
        
        forceRemoveUser(user);
    }
    
    public void forceRemoveUser(User user) throws Exception {
        String removedLogin = user.getLogin();
        
        removeUserSettings(user);
        removeUserResources(user);
        
        Long userSettingsId =  user.getUserSettings() != null ? user.getUserSettings().getId() : null;
        Query query = em.createQuery("DELETE FROM User WHERE id = ?");
        query.setParameter(1, user.getId());
        query.executeUpdate();
        
        if (user.getUserSettings() != null) {
            query = em.createQuery("DELETE FROM UserSettings WHERE id = ?");
            query.setParameter(1, userSettingsId.longValue());
            query.executeUpdate();
        }
        generateAuditLogForCreateRemoveUser(removedLogin, true);
        logger.info("{} removed {} account", requestUser.getLogin(), removedLogin);
        
        em.flush();
    }
    
    private void removeUserSettings(User user) throws Exception {
        Query query;
        
        query = em.createQuery("DELETE FROM NotificationSettings WHERE user = ?");
        query.setParameter(1, user);
        query.executeUpdate();
        
        //Remove user data from table users_notifications
        user.setNotificationEvents(Collections.EMPTY_SET);
        
        //Remove user data from table users_mobilenotifications        
        user.setMobileNotificationSettings(Collections.EMPTY_MAP);
        
        em.persist(user);
        
        query = em.createQuery("DELETE FROM UIStateEntry WHERE user = ?");
        query.setParameter(1, user);
        query.executeUpdate();
        
        query = em.createQuery("DELETE FROM UserDeviceStatus uds WHERE uds.id.user = ?");
        query.setParameter(1, user);
        query.executeUpdate();
        
        query = em.createQuery("DELETE FROM UserSession us WHERE us.userId = ?");
        query.setParameter(1, user.getId());
        query.executeUpdate();
    }
    
    private void removeUserResources(User user) throws Exception {
 
        DeviceGroupProvider deviceGroupProvider = new DeviceGroupProvider(em, user);        
        GeoFenceProvider geoProvider = new GeoFenceProvider(em);
        geoProvider.setRequestUser(user);
        
        //users
        user.setManagedUsers(Collections.EMPTY_SET);
        em.persist(user);
        
        Query query = em.createQuery("SELECT u FROM User u WHERE u.managedBy = :manager");
        query.setParameter("manager", user);
        for (User us : (List<User>) query.getResultList()) {
            us.setManagedBy(requestUser);
            logger.info("{} became manager of {}", requestUser.getLogin(), us.getLogin());
        }
        
        // devices
        user.setDevices(Collections.EMPTY_SET);
        em.persist(user);
        
        query = em.createQuery("SELECT d FROM Device d WHERE d.owner = :owner");
        query.setParameter("owner", user);
        for (Device dev : (List<Device>) query.getResultList()) {
            dev.setOwner(requestUser);
            logger.info("{} became owner of {}(id={})", requestUser.getLogin(),
                    dev.getName(), dev.getId());
        }
        em.flush();

        // tracks
        RouteProvider rp = new RouteProvider(em, user);
        query = em.createQuery("SELECT r FROM Route r wHERE r.owner = :owner");
        query.setParameter("owner", user);
        for(Route r : (List<Route>)query.getResultList())
            rp.forceDeleteRoute(r);
        
        // geofences
        query = em.createQuery("SELECT g FROM GeoFence g WHERE :user in elements(g.users)");
        query.setParameter("user", user);
        for (GeoFence geo : (List<GeoFence>) query.getResultList()) {
            if (geo.getOwner() == user) {
                geo.setUsers(new HashSet<>());
                em.remove(geo);
            }
            else {
                Set<User> us = geo.getUsers();
                us.remove(user);
                geo.setUsers(us);
            }
        }
        em.flush();
        
        // groups
        for (Group gr : user.getGroups()) {
            Set<User> us = gr.getUsers();
            if (us == null) 
                continue;
            if (us.size() == 1) {
                deviceGroupProvider.hardRemoveGroup(gr);
            }
            else {
                us.remove(user);
                gr.setUsers(us);
            }
        }
        
        // RulesAcceptances
        query = em.createQuery("DELETE FROM RulesAcceptance a WHERE a.id.user = :user");
        query.setParameter("user", user);
        query.executeUpdate();
        
        //BleDevices
        query = em.createQuery("DELETE FROM BleDevice bd where bd.owner = :user");
        query.setParameter("user", user);
        query.executeUpdate();
        
        //UserEvent
        query = em.createQuery("DELETE FROM UserEvent ue where ue.user = :user");
        query.setParameter("user", user);
        query.executeUpdate();
    }
    
    // REMOVING USER - END // 
    
    private void editUser(User user, EditUserDto dto) throws ProviderException {
        if(!requestUser.hasPermission(UserPermission.ALL_USERS) && user.equals(requestUser.getManagedBy()))
            throw new ProviderException(Type.ACCESS_DENIED);
        
        generateAuditLogEditUser(user, dto);
        
        user.setAdmin(false);
        user.setCompanyName(dto.getCompanyName());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setManager(true);
        if(requestUser.hasPermission(UserPermission.ALL_USERS) || !user.equals(requestUser)) {
            user.setMaxNumOfDevices(dto.getMaxNumOfDevices());
            user.setExpirationDate(dto.getExpirationDate());
        }
        Set<DeviceEventType> notificationEvents = new HashSet<>();
        for(String ev : dto.getNotificationEvents()) {
            notificationEvents.add(DeviceEventType.valueOf(ev));
        }
        user.setNotificationEvents(notificationEvents);
        user.setPhoneNumber(dto.getPhoneNumber());
        if (requestUser.getId() != user.getId()) { 
            user.setReadOnly(false);
            user.setArchive(true);
            user.setBlocked(dto.isBlocked());
        }
    }
    
    private void checkUserManagementPermissions(User user) throws ProviderException {
        if (!user.hasPermission(UserPermission.USER_MANAGEMENT))
            throw new ProviderException(ProviderException.Type.ACCESS_DENIED);
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
    
    public String requestPasswordReset(String login) throws ProviderException {
        User u = getUserByLogin(login);
        if (u == null) 
            throw new ProviderException(Type.NOT_FOUND);
        
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
        if(requestUser.hasPermission(UserPermission.ALL_USERS))
            return true;
        
        return getAllAvailableUsers().anyMatch(u -> u.equals(other));
    }

    public void updateUserSettings(long id, EditUserSettingsDto dto) throws ProviderException {
        if(id != requestUser.getId())
            throw new ProviderException(Type.ACCESS_DENIED);
        UserSettings us = requestUser.getUserSettings();
        if(dto.getArchiveMarkerType() != null && ! dto.getArchiveMarkerType().isEmpty())
            us.setArchiveMarkerType(PositionIconType.valueOf(dto.getArchiveMarkerType()));
        else
            us.setArchiveMarkerType(null);
        us.setCenterLatitude(dto.getCenterLatitude());
        us.setCenterLongitude(dto.getCenterLongitude());
        us.setFollowedDeviceZoomLevel(dto.getFollowedDeviceZoomLevel());
        us.setHideDuplicates(dto.isHideDuplicates());
        us.setHideInvalidLocations(dto.isHideInvalidLocations());
        us.setHideZeroCoordinates(dto.isHideZeroCoordinates());
        us.setMapType(UserSettings.MapType.valueOf(dto.getMapType()));
        us.setMaximizeOverviewMap(dto.isMaximizeOverviewMap());
        us.setMinDistance(dto.getMinDistance());
        us.setOverlays(dto.getOverlays());
        us.setSpeedForFilter(dto.getSpeedForFilter());
        us.setSpeedModifier(dto.getSpeedModifier());
        if(dto.getSpeedUnit() != null && !dto.getSpeedUnit().isEmpty())
            us.setSpeedUnit(SpeedUnit.valueOf(dto.getSpeedUnit()));
        us.setTimePrintInterval(dto.getTimePrintInterval());
        us.setTimeZoneId(dto.getTimeZoneId());
        us.setTraceInterval(dto.getTraceInterval());
        us.setZoomLevel(dto.getZoomLevel());
    }

    public UserSettings getUserSettings(long id) throws ProviderException {
        if(id != requestUser.getId())
            throw new ProviderException(Type.ACCESS_DENIED);
        return requestUser.getUserSettings();
    }
    
    public void updateUserBleDevices(long id, List<BleDeviceDto> dtos) throws ProviderException {
        if(id != requestUser.getId())
            throw new ProviderException(Type.ACCESS_DENIED);
        if(!dtos.isEmpty()) {
            requestUser.setWasBleUser(true);
            requestUser.setHadAnyDevice(true);
        }
        
        Query q = em.createQuery("DELETE FROM BleDevice bd where bd.owner = :user");
        q.setParameter("user", requestUser);
        q.executeUpdate();
        
        List<BleDevice> bled = new ArrayList<>();
        for(BleDeviceDto dto : dtos) {
            BleDevice bd = new BleDevice();
            bd.setModelName(dto.getModelName());
            bd.setCreationTimestamp(dto.getCreationTimestamp());
            bd.setPhoneModel(dto.getPhoneModel().toLowerCase());
            bd.setPhoneSystem(dto.getPhoneSystem().toLowerCase());
            bd.setOwner(requestUser);
            em.persist(bd); 
            bled.add(bd);
        }
        requestUser.updateBleDevices(bled);
        em.persist(requestUser);
    }
                
    
    // AuditLog methods
    
    private void generateAuditLogForCreateRemoveUser(String userLogin, boolean remove) {
        AuditLog al = new AuditLog.Builder()
                .agentLogin(requestUser != null ? requestUser.getLogin() : "REGISTRATION")
                .type(remove ? AuditLogType.REMOVED_USER : AuditLogType.CREATED_USER)
                .targetUserLogin(userLogin)
                .build();
        
        em.persist(al);
    }
    
    private void generateAuditLogEditUser(User user, EditUserDto dto) {
        if (!Objects.equals(user.getEmail(), dto.getEmail()))
            addSingleEditUserAuditLog(user.getLogin(), "email", dto.getEmail());
        if (!Objects.equals(user.getFirstName(), dto.getFirstName()))
            addSingleEditUserAuditLog(user.getLogin(), "firstName", dto.getFirstName());
        if (!Objects.equals(user.getLastName(), dto.getLastName()))
            addSingleEditUserAuditLog(user.getLogin(), "lastName", dto.getLastName());
        if (!Objects.equals(user.getMaxNumOfDevices(), dto.getMaxNumOfDevices())) {
            String maxDevStr = dto.getMaxNumOfDevices() == null ? 
                    "null" : dto.getMaxNumOfDevices().toString();
            addSingleEditUserAuditLog(user.getLogin(), "maxNumOfDevices", maxDevStr);
        }
        if (!Objects.equals(user.getExpirationDate(), dto.getExpirationDate())) {
            String dateStr = dto.getExpirationDate() == null ? 
                    "null" : dto.getExpirationDate().toString();
            addSingleEditUserAuditLog(user.getLogin(), "expirationDate", dateStr);
        }
        if (!Objects.equals(user.isBlocked(), dto.isBlocked()))
            addSingleEditUserAuditLog(user.getLogin(), "blocked", dto.isBlocked() ? "true" : "false");  
    }
    
    private void addSingleEditUserAuditLog(String userLogin, String fieldName, String fieldNewValue) {
        AuditLog al = new AuditLog.Builder()
                .agentLogin(requestUser.getLogin())
                .type(AuditLogType.CHANGED_USER)
                .targetUserLogin(userLogin)
                .fieldName(fieldName)
                .fieldNewValue(fieldNewValue)
                .build();
        
        em.persist(al);
    }
}
