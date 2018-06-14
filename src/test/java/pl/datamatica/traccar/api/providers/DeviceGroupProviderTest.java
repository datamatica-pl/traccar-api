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

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.Group;

/**
 *
 * @author piotrkrzeszewski
 */
public class DeviceGroupProviderTest  {
    
    private DeviceGroupProvider provider;
    private ApplicationSettings settings;
    private static TestDatabase database;
    private static EntityManager em;
    
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
        settings = new ApplicationSettings();
    }
    
    @Test
    public void getAll_admin() throws ProviderException {
        provider = new DeviceGroupProvider(em, database.admin);
        provider.setDeviceProvider(new DeviceProvider(em, database.admin, 
                new ImeiProvider(em), provider, null, settings));
        
        List<Group> result = provider.getAllAvailableGroups().collect(Collectors.toList());
        
        assertEquals(3, result.size());
        result.forEach((Group g) -> assertTrue(g.isOwned() == true));
    }
    
    @Test
    public void getAll_managedUser() throws ProviderException {
        provider = new DeviceGroupProvider(em, database.managedUser);
        provider.setDeviceProvider(new DeviceProvider(em, database.managedUser, 
                new ImeiProvider(em), provider, null, settings));
        
        List<Group> result = provider.getAllAvailableGroups().collect(Collectors.toList());
        
        assertEquals(2, result.size());
        assertTrue(result.contains(database.managedDeviceDeviceGroup));
        assertTrue(result.contains(database.managed2DeviceGroup));
        assertTrue(result.get(result.indexOf(database.managedDeviceDeviceGroup)).isOwned());
        assertTrue(!result.get(result.indexOf(database.managed2DeviceGroup)).isOwned());
    }
    
    @Test
    public void getAll_managed2User() throws ProviderException {
        provider = new DeviceGroupProvider(em, database.managed2);
        provider.setDeviceProvider(new DeviceProvider(em, database.managed2, 
                new ImeiProvider(em), provider, null, settings));
        
        List<Group> result = provider.getAllAvailableGroups().collect(Collectors.toList());
        
        assertEquals(2, result.size());
        assertTrue(result.contains(database.managedDeviceDeviceGroup));
        assertTrue(result.contains(database.managed2DeviceGroup));
        assertTrue(result.get(result.indexOf(database.managedDeviceDeviceGroup)).isOwned());
        assertTrue(result.get(result.indexOf(database.managed2DeviceGroup)).isOwned());
    }
    
    @Test
    public void getAll_managed3User() throws ProviderException {
        provider = new DeviceGroupProvider(em, database.managed3);
        provider.setDeviceProvider(new DeviceProvider(em, database.managed3, 
                new ImeiProvider(em), provider, null, settings));
        
        List<Group> result = provider.getAllAvailableGroups().collect(Collectors.toList());
        
        assertEquals(1, result.size());
        assertTrue(result.contains(database.adminDeviceGroup));
        assertTrue(!result.get(result.indexOf(database.adminDeviceGroup)).isOwned());
    }
    
    @Test 
    public void delete_accessDanied() throws ProviderException {
        provider = new DeviceGroupProvider(em, database.managed3);
        provider.setDeviceProvider(new DeviceProvider(em, database.managed3, 
                new ImeiProvider(em), provider, null, settings));
        
        try {
            provider.deleteGroup(database.adminDeviceGroup.getId());
        } catch(ProviderException e) {
            assertEquals(ProviderException.Type.ACCESS_DENIED, e.getType());
            return;
        }
        fail();
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