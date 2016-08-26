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

import javax.persistence.*;
import org.junit.*;
import static org.junit.Assert.*;
import pl.datamatica.traccar.api.providers.ProviderException.*;
import pl.datamatica.traccar.model.Position;

public class PositionProviderTest {
    
    private PositionProvider provider;
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
    }
    
    @Test
    public void get_ok() throws ProviderException {
        provider = new PositionProvider(em, database.admin);
        Position expected = database.adminPosition;
       
        Position position = provider.get(expected.getId());
        
        assertEquals(expected, position);
    }
    
    @Test
    public void get_notFound() {
        provider = new PositionProvider(em, database.admin);
        try {
            provider.get(859);
        } catch (ProviderException ex) {
            assertEquals(Type.NOT_FOUND, ex.getType());
            return;
        }
        fail();
    }
    
    @Test
    public void get_accessDenied() {
        provider = new PositionProvider(em, database.managed2);
        try {
            provider.get(database.adminPosition.getId());
        } catch(ProviderException e) {
            assertEquals(Type.ACCESS_DENIED, e.getType());
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
