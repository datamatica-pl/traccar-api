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

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.GeoFenceType;
import pl.datamatica.traccar.model.Group;
import pl.datamatica.traccar.model.PasswordHashMethod;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserGroup;
import pl.datamatica.traccar.model.UserPermission;

public class TestDatabase {
    private final EntityManager em;
    private String salt;
    ApplicationSettings applicationSettings;
    User admin;
    Device adminDevice;
    Position adminPosition;
    User manager;
    Device managerDevice;
    User managedUser;
    Device managedDevice;
    User managed2;
    User managed3;
    Device managed2Device;
    GeoFence adminGeofence;
    GeoFence managedUserGeofence;
    Group adminDeviceGroup;
    Group managedDeviceDeviceGroup;
    Group managed2DeviceGroup;
    UserGroup usersGroup;
    UserGroup adminsGroup;
    List<Position> managed2Positions;
    
    public TestDatabase(EntityManager em) {
        this.em = em;
        salt = "5dj32";
    }
    
    public void createDatabase() {
        em.getTransaction().begin();
        createApplicationSettings();
        createAdmin();
        createManager();
        createManagedUser();
        createManaged2();
        createManaged3();
        createDeviceGroups();
        createUserGroups();
        createDevices();
        createPositions();
        createGeofences();
        createPositionsManaged2Device();
        em.getTransaction().commit();
    }
    
    private void createApplicationSettings() {
        applicationSettings = new ApplicationSettings();
        applicationSettings.setSalt(salt);
        em.persist(applicationSettings);
    }
    
    private void createAdmin() {
        admin = new User();
        admin.setLogin("admin@admin.pl");
        admin.setEmail("admin@admin.pl");
        admin.setPassword("zażółć gęślą jaźń");
        admin.setPasswordHashMethod(PasswordHashMethod.PLAIN);
        admin.setAdmin(true);
        em.persist(admin);
    }
    
    private void createManager() {
        manager = new User();
        manager.setLogin("manager@test.pl");
        manager.setEmail("manager@test.pl");
        manager.setPassword(PasswordHashMethod.MD5.doHash("Test11!", salt));
        manager.setManager(true);
        em.persist(manager);
    }

    private void createManagedUser() {
        managedUser = new User();
        managedUser.setLogin("user1@test.pl");
        managedUser.setEmail("user1@test.pl");
        managedUser.setPassword("user_1");
        managedUser.setPasswordHashMethod(PasswordHashMethod.PLAIN);
        managedUser.setManagedBy(manager);
        managedUser.setManager(true);
        manager.setManagedUsers(Collections.singleton(managedUser));
        em.persist(managedUser);
    }
    
    private void createManaged2() {
        managed2 = new User();
        managed2.setLogin("managed2@test.pl");
        managed2.setEmail("managed2@test.pl");
        managed2.setPassword(PasswordHashMethod.MD5.doHash("managed_2", salt));
        managed2.setManagedBy(managedUser);
        managedUser.setManagedUsers(Collections.singleton(managed2));
        em.persist(managed2);
    }
    
    //this user will have access to adminDevice - to check if he'll get proper groups
    private void createManaged3() {
        managed3 = new User();
        managed3.setLogin("managed3@test.pl");
        managed3.setEmail("managed3@test.pl");
        managed3.setPassword(PasswordHashMethod.MD5.doHash("managed_3", salt));
        em.persist(managed3);
    }

    private void createDeviceGroups() {
        adminDeviceGroup = new Group();
        adminDeviceGroup.setName("Admins group");
        em.persist(adminDeviceGroup);
        
        managedDeviceDeviceGroup = new Group();
        managedDeviceDeviceGroup.setName("ManagedDevice's Group, shared with managed2");
        em.persist(managedDeviceDeviceGroup);
        
        managed2DeviceGroup = new Group();
        managed2DeviceGroup.setName("Managed2Users, not shared");
        em.persist(managed2DeviceGroup);
        
        admin.setGroups(Collections.singleton(adminDeviceGroup));
        managedUser.setGroups(Collections.singleton(managedDeviceDeviceGroup));
        managed2.setGroups(Stream.of(managedDeviceDeviceGroup, managed2DeviceGroup).collect(Collectors.toSet()));
    }
    
    private void createUserGroups() {
        usersGroup = new UserGroup();
        usersGroup.setName("users");
        usersGroup.setPermissions(UserPermission.getUsersPermissions());
        em.persist(usersGroup);
        
        adminsGroup = new UserGroup();
        adminsGroup.setName("admins");
        adminsGroup.setPermissions(UserPermission.getAdminsPermissions());
        em.persist(adminsGroup);
        
        applicationSettings.setDefaultGroup(usersGroup);
        
        admin.setUserGroup(adminsGroup);
        manager.setUserGroup(usersGroup);
        managedUser.setUserGroup(usersGroup);
        managed2.setUserGroup(usersGroup);
        managed3.setUserGroup(usersGroup);
        
        em.flush();
    }
    
    private void createDevices() {
        managerDevice = new Device();
        managerDevice.setUniqueId("20");
        managerDevice.setOwner(manager);
        manager.setDevices(Collections.singleton(managerDevice));
        em.persist(managerDevice);
        
        managedDevice = new Device();
        managedDevice.setUniqueId("10");
        managedDevice.setOwner(managedUser);
        managedDevice.setDeleted(true);
        managedDevice.setGroup(managedDeviceDeviceGroup);
        managedUser.setDevices(Collections.singleton(managedDevice));
        em.persist(managedDevice);
        
        adminDevice = new Device();
        adminDevice.setUniqueId("30");
        adminDevice.setOwner(admin);
        adminDevice.setGroup(adminDeviceGroup);
        admin.setDevices(Collections.singleton(adminDevice));
        em.persist(adminDevice);
        
        managed2Device = new Device();
        managed2Device.setUniqueId("5");
        managed2Device.setOwner(managed2);
        managed2Device.setGroup(managed2DeviceGroup);
        managed2.setDevices(Collections.singleton(managed2Device));
        em.persist(managed2Device);
        
        managed3.setDevices(Collections.singleton(adminDevice));
    }

    private void createPositions() {
        adminPosition = new Position();
        adminPosition.setLatitude(52.);
        adminPosition.setLongitude(19.);
        adminPosition.setDevice(adminDevice);
        em.persist(adminPosition);
    }
    
    // Creates positions for managed2device. This device shouldn't gen any more positions. 
    // This data will be used in device/<id>/positions filter tests
    private void createPositionsManaged2Device() {
        final long MINUTE = 60000;//millisecs
        
        Calendar date = Calendar.getInstance();
        long baseTime = date.getTimeInMillis() - 60 * MINUTE;

        em.persist(preparePosition(51., 21., managed2Device, new Date(baseTime + 10 * MINUTE)));
        em.persist(preparePosition(52., 21., managed2Device, new Date(baseTime + 20 * MINUTE)));
        em.persist(preparePosition(53., 21., managed2Device, new Date(baseTime + 30 * MINUTE)));
    }
    
    private Position preparePosition(double lat, double lon, Device dev, Date date) {
        Position pos = new Position();
        pos.setLatitude(lat);
        pos.setLongitude(lon);
        pos.setDevice(dev);
        pos.setTime(date);
        pos.setServerTime(date);
        return pos;
    }
    
    private void createGeofences() {
        adminGeofence = new GeoFence();
        adminGeofence.setType(GeoFenceType.CIRCLE);
        adminGeofence.setName("Zażółć gęślą jaźń");
        adminGeofence.setUsers(Collections.singleton(admin));
        em.persist(adminGeofence);
        
        managedUserGeofence = new GeoFence();
        managedUserGeofence.setType(GeoFenceType.LINE);
        managedUserGeofence.setName("ManagedUser's geofence");
        managedUserGeofence.setUsers(Collections.singleton(managedUser));
        em.persist(managedUser);
    }

    public void prepareEm(EntityManager newEm) {
        admin = newEm.find(User.class, admin.getId());
        manager = newEm.find(User.class, manager.getId());
        managedUser = newEm.find(User.class, managedUser.getId());
        managed2 = newEm.find(User.class, managed2.getId());
        adminDevice = newEm.find(Device.class, adminDevice.getId());
        managerDevice = newEm.find(Device.class, managerDevice.getId());
        managedDevice = newEm.find(Device.class, managedDevice.getId());
        managed2Device = newEm.find(Device.class, managed2Device.getId());
        adminGeofence = newEm.find(GeoFence.class, adminGeofence.getId());
        adminPosition = newEm.find(Position.class, adminPosition.getId());
    }
}
