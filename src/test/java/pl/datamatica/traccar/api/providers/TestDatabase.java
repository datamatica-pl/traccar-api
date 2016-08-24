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

import java.util.Collections;
import javax.persistence.EntityManager;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.PasswordHashMethod;
import pl.datamatica.traccar.model.User;

public class TestDatabase {
    private final EntityManager em;
    private String salt;
    User admin;
    Device adminDevice;
    User manager;
    Device managerDevice;
    User managedUser;
    Device managedDevice;
    User managed2;
    Device managed2Device;
    
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
        createDevices();
        em.getTransaction().commit();
    }
    
    private ApplicationSettings createApplicationSettings() {
        ApplicationSettings as = new ApplicationSettings();
        as.setSalt(salt);
        em.persist(as);
        return as;
    }
    
    private void createAdmin() {
        admin = new User();
        admin.setEmail("admin@admin.pl");
        admin.setLogin("admin");
        admin.setPassword("zażółć gęślą jaźń");
        admin.setPasswordHashMethod(PasswordHashMethod.PLAIN);
        admin.setAdmin(true);
        em.persist(admin);
    }
    
    private void createManager() {
        manager = new User();
        manager.setEmail("manager@test.pl");
        manager.setLogin("manager");
        manager.setPassword(PasswordHashMethod.MD5.doHash("Test11!", salt));
        manager.setManager(true);
        em.persist(manager);
    }

    private void createManagedUser() {
        managedUser = new User();
        managedUser.setLogin("user #1");
        managedUser.setEmail("user1@test.pl");
        managedUser.setPassword("user_1");
        managedUser.setPasswordHashMethod(PasswordHashMethod.PLAIN);
        managedUser.setManagedBy(manager);
        managedUser.setManager(true);
        manager.setManagedUsers(Collections.singleton(managedUser));
        em.persist(managedUser);
    }
    
    public void createManaged2() {
        managed2 = new User();
        managed2.setLogin("user #1#1");
        managed2.setLogin("user #1#1");
        managed2.setEmail("managed2@test.pl");
        managed2.setPassword(PasswordHashMethod.MD5.doHash("managed_2", salt));
        managed2.setManagedBy(managedUser);
        managedUser.setManagedUsers(Collections.singleton(managed2));
        em.persist(managed2);
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
        managedUser.setDevices(Collections.singleton(managedDevice));
        em.persist(managedDevice);
        
        adminDevice = new Device();
        adminDevice.setUniqueId("30");
        adminDevice.setOwner(admin);
        admin.setDevices(Collections.singleton(adminDevice));
        em.persist(adminDevice);
        
        managed2Device = new Device();
        managed2Device.setUniqueId("5");
        managed2Device.setOwner(managed2);
        managed2.setDevices(Collections.singleton(managed2Device));
        em.persist(managed2Device);
    }
}
