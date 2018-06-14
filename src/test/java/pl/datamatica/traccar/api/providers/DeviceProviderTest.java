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
import org.mockito.Mockito;
import pl.datamatica.traccar.api.dtos.in.EditDeviceDto;
import pl.datamatica.traccar.api.metadata.model.DeviceModel;
import pl.datamatica.traccar.api.metadata.model.ImeiNumber;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;

public class DeviceProviderTest {
    
    private DeviceProvider provider;
    private static EntityManager em;
    private static TestDatabase database;
    private ImeiProvider imeiProvider;
    private boolean isImeiValid;
    private final static long DEFAULT_NOT_EXISTENT_MODEL_ID = -1L;
    private ApplicationSettings settings;
    
    @BeforeClass
    public static void classInit() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test");
        em = emf.createEntityManager();
        database = new TestDatabase(em);
        database.createDatabase();
    }
    
    @Before
    public void testInit() {
        imeiProvider = Mockito.mock(ImeiProvider.class);
        settings = new ApplicationSettings();
        settings.setDefaultIconId(0);
        
        ImeiNumber imei = new ImeiNumber();
        imei.setImei("999888777666001");
        imei.setDeviceModel("GT100/MT200");
        Mockito.when(imeiProvider.getImeiByImeiString(Mockito.anyString())).thenReturn(imei);
        
        em.getTransaction().begin();
        em.getTransaction().setRollbackOnly();
    }
    
    @Test
    public void getDevice_ok() throws ProviderException {  
        provider = new DeviceProvider(em, database.manager, imeiProvider, 
                null, null, settings);
        Device expected = database.managed2Device;
        
        Device actual = provider.getDevice(expected.getId());
        
        assertEquals(expected, actual);
    }
    
    @Test
    public void getDevice_notFound() {
        provider = new DeviceProvider(em, database.admin, imeiProvider, null, null, settings);
        try {
            provider.getDevice(999);
        } catch(ProviderException e) {
            assertEquals(ProviderException.Type.NOT_FOUND, e.getType());
            return;
        }
        fail();
    }
    
    @Test
    public void getDevice_accesDenied() {
        provider = new DeviceProvider(em, database.managed2, imeiProvider, null, null, settings);
        try {
            provider.getDevice(database.adminDevice.getId());
        } catch(ProviderException e) {
            assertEquals(ProviderException.Type.ACCESS_DENIED, e.getType());
            return;
        }
        fail();
    }
    
    @Test 
    public void getAllAvailableDevices_admin() {
        provider = new DeviceProvider(em, database.admin, imeiProvider, null, null, settings);
        Set<Device> expected = Stream.of(database.managedDevice, 
                database.managed2Device,
                database.adminDevice,
                database.managerDevice)
                .collect(Collectors.toSet());
        
        Set<Device> actual = provider.getAllAvailableDevices()
                .collect(Collectors.toSet());
        assertEquals(expected, actual);
    }
    
    @Test
    public void getAllAvailableDevices_manager() {
        provider = new DeviceProvider(em, database.manager, imeiProvider, null, 
                null, settings);
        Set<Device> expected = Stream.of(database.managerDevice, 
                database.managedDevice,
                database.managed2Device)
                .collect(Collectors.toSet());
               
        Set<Device> actual = provider.getAllAvailableDevices()
                .collect(Collectors.toSet());
        assertEquals(expected, actual);
    }
    
    @Test
    public void createDevice_ok() throws ProviderException {
        String uniqueId = "999888777666001";
        User user = database.admin;
        long iconId = 73;
        settings.setDefaultIconId((int)iconId);
        provider = new DeviceProvider(em, user, imeiProvider, null, null, settings);

        Device device = provider.createDevice(uniqueId, null);
        em.flush();
        
        assertNotNull(device);
        assertEquals(uniqueId, device.getUniqueId());
        assertEquals(user, device.getOwner());
        assertEquals(DEFAULT_NOT_EXISTENT_MODEL_ID, device.getDeviceModelId());
        assertEquals("0000FF", device.getColor());
        assertFalse(device.isDeleted());
        assertNotNull(device.getIconType());
        assertTrue(device.getUsers().contains(user));
        assertEquals((Long)iconId, device.getIconId());
    }
    
    @Test
    public void createDevice_model_id_automatically_added() throws ProviderException {
        final Long expectedModelId = 3L;
        User testUser = Mockito.mock(User.class);
        Mockito.when(testUser.hasPermission(Mockito.anyObject())).thenReturn(Boolean.TRUE);
        provider = new DeviceProvider(em, testUser, imeiProvider, null, null, settings);

        DeviceModelProvider modelProvider = Mockito.mock(DeviceModelProvider.class);
        DeviceModel devModelTest = new DeviceModel();
        devModelTest.setId(expectedModelId);

        Mockito.when(modelProvider.getDeviceModelLike(Mockito.anyString())).thenReturn(devModelTest);

        try {
            Long modelId = provider.createDevice("999888777666011", modelProvider).getDeviceModelId();
            assertEquals(modelId, expectedModelId);
        } catch (ProviderException e) {
            fail("ProviderExcepton: " + e.getMessage());
        }
    }
    
    @Test
    public void createDevice_deviceModel_null() throws ProviderException {
        User testUser = Mockito.mock(User.class);
        Mockito.when(testUser.hasPermission(Mockito.anyObject())).thenReturn(Boolean.TRUE);
        provider = new DeviceProvider(em, testUser, imeiProvider, null, null, settings);

        DeviceModelProvider modelProvider = Mockito.mock(DeviceModelProvider.class);

        Mockito.when(modelProvider.getDeviceModelLike(Mockito.anyString())).thenReturn(null);

        try {
            long modelId = provider.createDevice("999888777666011", modelProvider).getDeviceModelId();
            assertEquals(modelId, DEFAULT_NOT_EXISTENT_MODEL_ID);
        } catch (ProviderException e) {
            fail("ProviderExcepton: " + e.getMessage());
        }
    }
    
    @Test
    public void createDevice_deviceModelProvider_null() throws ProviderException {
        User testUser = Mockito.mock(User.class);
        Mockito.when(testUser.hasPermission(Mockito.anyObject())).thenReturn(Boolean.TRUE);
        provider = new DeviceProvider(em, testUser, imeiProvider, null, null, settings);

        try {
            long modelId = provider.createDevice("999888777666011", null).getDeviceModelId();
            assertEquals(modelId, DEFAULT_NOT_EXISTENT_MODEL_ID);
        } catch (ProviderException e) {
            fail("ProviderExcepton: " + e.getMessage());
        }
    }
    
    @Test
    public void createDevice_imeiExists() {
        provider = new DeviceProvider(em, database.manager, imeiProvider, null, null, settings);
        try{
            String uniqueId = database.managerDevice.getUniqueId();

            provider.createDevice(uniqueId, null);
        } catch(ProviderException e) {
            assertEquals(ProviderException.Type.DEVICE_ALREADY_EXISTS, e.getType());
            return;
        }
        fail();
    }
    
    @Test
    public void createDevice_deletedImei() throws ProviderException {
        String uniqueId = database.managedDevice.getUniqueId();
        User user = database.managed2;
        provider = new DeviceProvider(em, user, imeiProvider, null, null, settings);
        
        Device device = provider.createDevice(uniqueId, null);
        
        assertTrue(device.getId() > 0);
        assertEquals(uniqueId, device.getUniqueId());
        assertNull(device.getLatestPosition());
        assertFalse(device.isDeleted());
        assertTrue(device.getUsers().contains(user));
    }
    
    @Test
    public void updateDevice_ok() throws ProviderException {
        String color = "FF00FF";
        String description = "Lorem ipsum";
        long modelId = 5;
        String name = "test";
        long iconId = 3;
        String phoneNumber = "123456789";
        String plateNumber = "EL 75843";
        
        EditDeviceDto deviceDto = new EditDeviceDto.Builder()
                .color(color)
                .description(description)
                .deviceModelId(modelId)
                .deviceName(name)
                .iconId(iconId)
                .phoneNumber(phoneNumber)
                .plateNumber(plateNumber)
                .build();
        provider = new DeviceProvider(em, database.managedUser, imeiProvider, null, null, 
                settings);
        
        provider.updateDevice(database.managedDevice.getId(), deviceDto);
        em.flush();
        Device actual = em.find(Device.class, database.managedDevice.getId());
        
        assertEquals(color, actual.getColor());
        assertEquals(description, actual.getDescription());
        assertEquals(modelId, actual.getDeviceModelId());
        assertEquals(name, actual.getName());
        assertEquals(iconId, (long)actual.getIconId());
        assertEquals(phoneNumber, actual.getPhoneNumber());
        assertEquals(plateNumber, actual.getPlateNumber());
    }
    
    @Test
    public void delete_ok() throws ProviderException {
        provider = new DeviceProvider(em, database.managed2, imeiProvider, null, 
                null, settings);
        provider.delete(database.managed2Device.getId());
        em.flush();
        
        Device updated = em.find(Device.class, database.managed2Device.getId());
        assertTrue(updated.isDeleted());
    }
    
    @Test
    public void delete_accessDenied() {
        provider = new DeviceProvider(em, database.managed2, imeiProvider, null,
                null, settings);
        try {
            provider.delete(database.adminDevice.getId());
        } catch(ProviderException e) {
            assertEquals(ProviderException.Type.ACCESS_DENIED, e.getType());
            return;
        }
        fail();
    }
    
    @Test
    public void delete_alreadyDeleted() {
        provider = new DeviceProvider(em, database.managedUser, imeiProvider, 
                null, null, settings);
        try {
            provider.delete(database.managedDevice.getId());
        } catch (ProviderException e) {
            assertEquals(ProviderException.Type.ALREADY_DELETED, e.getType());
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
