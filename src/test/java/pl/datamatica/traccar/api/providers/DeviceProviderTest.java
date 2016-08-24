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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.*;
import static org.junit.Assert.*;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;

public class DeviceProviderTest {
    
    private DeviceProvider provider;
    private static EntityManager em;
    private static TestDatabase database;
    
    @BeforeClass
    public static void classInit() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test");
        em = emf.createEntityManager();
        database = new TestDatabase(em);
        database.createDatabase();
    }
    
    @Before
    public void testInit() {
        em.getTransaction().begin();
        this.provider = new DeviceProvider(em);
    }
    
    @Test
    public void getDevice() {
        Device expected = new Device();
        em.persist(expected);
        em.flush();
        
        Device actual = provider.getDevice(expected.getId());
        
        assertEquals(expected, actual);
    }
    
    @Test 
    public void getAllAvailableDevices_admin() {
        Set<Device> expected = Stream.of(database.managedDevice, 
                database.managed2Device,
                database.adminDevice,
                database.managerDevice)
                .collect(Collectors.toSet());
        
        provider.setRequestUser(database.admin);
        Set<Device> actual = provider.getAllAvailableDevices()
                .collect(Collectors.toSet());
        assertEquals(expected, actual);
    }
    
    @Test
    public void getAllAvailableDevices_manager() {
        Set<Device> expected = Stream.of(database.managerDevice, 
                database.managedDevice,
                database.managed2Device)
                .collect(Collectors.toSet());
               
        provider.setRequestUser(database.manager);
        Set<Device> actual = provider.getAllAvailableDevices()
                .collect(Collectors.toSet());
        assertEquals(expected, actual);
    }
    
    @Test
    public void isVisible_admin() {
        provider.setRequestUser(database.admin);
        
        assertTrue(provider.isVisible(database.managerDevice));
        assertTrue(provider.isVisible(database.adminDevice));
    }
    
    @Test
    public void isVisible_manager() {
        provider.setRequestUser(database.manager);
        
        assertTrue(provider.isVisible(database.managerDevice));
        assertTrue(provider.isVisible(database.managedDevice));
        assertTrue(provider.isVisible(database.managed2Device));
        assertFalse(provider.isVisible(database.adminDevice));
    }
    
    @Test
    public void createDevice() {
        String uniqueId = "584930";
        User user = database.admin;
        
        provider.setRequestUser(user);
        Device device = provider.createDevice(uniqueId);
        em.flush();
        
        assertNotNull(device);
        assertEquals(uniqueId, device.getUniqueId());
        assertEquals(user, device.getOwner());
        assertEquals(-1, device.getDeviceModelId());
        assertEquals("0000FF", device.getColor());
        assertFalse(device.isDeleted());
        assertNotNull(device.getIconType());
        assertTrue(device.getUsers().contains(user));
    }
    
    @After
    public void testCleanup() {
        em.getTransaction().rollback();
    }
    
    @AfterClass
    public static void classCleanup() {
        em.close();
    }
    
}
