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
import javax.persistence.*;
import org.junit.*;
import static org.junit.Assert.*;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.model.GeoFence;

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
    public void getAllGeoFences() {
        provider.setRequestUser(database.admin);
        List<GeoFence> geofences = provider.getAllAvailableGeoFences()
                .collect(Collectors.toList());
        
        assertEquals(1, geofences.size());
        assertEquals(database.adminGeofence, geofences.get(0));
    }
}
