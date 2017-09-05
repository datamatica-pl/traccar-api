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
import java.util.stream.Stream;
import javax.persistence.*;
import org.junit.*;
import static org.junit.Assert.*;
import pl.datamatica.traccar.api.dtos.in.AddGeoFenceDto;
import pl.datamatica.traccar.api.dtos.out.PointDto;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.GeoFenceType;

public class GeofenceProviderTest {
    
    private static EntityManagerFactory emf;
    private static TestDatabase database;
    private EntityManager em;
    private GeoFenceProvider provider;
    
    @BeforeClass
    public static void classInit() {
        emf = Persistence.createEntityManagerFactory("test");
        EntityManager em = emf.createEntityManager();
        database = new TestDatabase(em);
        database.createDatabase();        
    }
    
    @Before
    public void testInit() {
        em = emf.createEntityManager();
        em.getTransaction().begin();
        database.prepareEm(em);
        provider = new GeoFenceProvider(em);
    }
    
    @Test
    public void getGeoFence_ok() throws ProviderException {
        provider.setRequestUser(database.admin);
        GeoFence expected = database.adminGeofence;
        
        GeoFence actual = provider.getGeoFence(expected.getId());
        
        assertEquals(expected, actual);
    }
    
    @Test
    public void getGeoFence_notFound() {
        provider.setRequestUser(database.admin);
        try {
            provider.getGeoFence(-5);
        } catch(ProviderException e) {
            assertEquals(Type.NOT_FOUND, e.getType());
            return;
        }
        fail();
    }
    
    @Test
    public void getGeoFence_accessDenied() {
        provider.setRequestUser(database.managedUser);
        try {
            provider.getGeoFence(database.adminGeofence.getId());
        } catch (ProviderException ex) {
            assertEquals(Type.ACCESS_DENIED, ex.getType());
            return;
        }
        fail();
    }
    
    @Test
    public void getAllGeoFences() throws ProviderException {
        provider.setRequestUser(database.admin);
        List<GeoFence> geofences = provider.getAllAvailableGeoFences()
                .collect(Collectors.toList());
        
        assertEquals(1, geofences.size());
        assertEquals(database.adminGeofence, geofences.get(0));
    }
    
    @Test
    public void createGeoFence_ok() throws ProviderException {
        List<PointDto> points = Stream.of(new PointDto(20, 52), new PointDto(21, 52), new PointDto(20, 51))
                .collect(Collectors.toList());
        
        AddGeoFenceDto geoFence = new AddGeoFenceDto.Builder()
                .allDevices(false)
                .color("00FE00")
                .description("Testowy opis")
                .geofenceName("testowa nazwa")
                .points(points)
                .type("POLYGON")
                .deviceIds(new long[]{database.adminDevice.getId()})
                .build();
        provider.setRequestUser(database.admin);
        GeoFence gf = provider.createGeoFence(geoFence);
        
        assertNotNull(gf);
        assertEquals(geoFence.getColor(), gf.getColor());
        assertEquals(geoFence.getDescription(), gf.getDescription());
        assertEquals(geoFence.getGeofenceName(), gf.getName());
        assertEquals(geoFence.getPointsString(), gf.getPoints());
        assertEquals(GeoFenceType.valueOf(geoFence.getType()), gf.getType());
        assertTrue(gf.getDevices().contains(database.adminDevice));
    }
    
    @Test
    public void updateGeoFence_ok() throws ProviderException {
        List<PointDto> points = Stream.of(new PointDto(20, 52), new PointDto(21, 52), new PointDto(20, 51))
                .collect(Collectors.toList());
        
        AddGeoFenceDto geoFence = new AddGeoFenceDto.Builder()
                .allDevices(false)
                .color("00FE00")
                .description("Testowy opis")
                .geofenceName("testowa nazwa")
                .points(points)
                .type("POLYGON")
                .deviceIds(new long[]{database.adminDevice.getId()})
                .build();
        provider.setRequestUser(database.admin);
        provider.updateGeoFence(database.adminGeofence.getId(), geoFence);
        em.flush();
        
        GeoFence actual = em.find(GeoFence.class, database.adminGeofence.getId());
        assertEquals(geoFence.getColor(), actual.getColor());
        assertEquals(geoFence.getDescription(), actual.getDescription());
        assertEquals(geoFence.getGeofenceName(), actual.getName());
        assertEquals(geoFence.getPointsString(), actual.getPoints());
        assertEquals(GeoFenceType.valueOf(geoFence.getType()), actual.getType());
        assertTrue(actual.getDevices().contains(database.adminDevice));
    }
}
