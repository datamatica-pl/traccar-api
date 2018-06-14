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
import java.util.Date;
import java.util.stream.Stream;
import javax.persistence.*;
import org.junit.*;
import static org.junit.Assert.*;
import pl.datamatica.traccar.api.providers.ProviderException.*;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.Position;

public class PositionProviderTest {
    
    private PositionProvider provider;
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
    public void get_ok() throws ProviderException {
        provider = new PositionProvider(em, database.admin, settings);
        Position expected = database.adminPosition;
       
        Position position = provider.get(expected.getId());
        
        assertEquals(expected, position);
    }
    
    @Test
    public void get_notFound() {
        provider = new PositionProvider(em, database.admin, settings);
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
        provider = new PositionProvider(em, database.managed2, settings);
        try {
            provider.get(database.adminPosition.getId());
        } catch(ProviderException e) {
            assertEquals(Type.ACCESS_DENIED, e.getType());
            return;
        }
        fail();
    }
    
    @Test
    public void getAllAvailablePositions_success() throws ProviderException {
        provider = new PositionProvider(em, database.manager, settings);
        
        Position position = new Position();
        position.setLatitude(1.);
        position.setLongitude(1.);
        position.setTime(new Date());
        position.setServerTime(new Date());
        position.setValid(true);
        position.setValidStatus(Position.VALID_STATUS_ALARM);
        position.setDevice(database.managerDevice);
        
        Position position2 = new Position();
        position2.setLatitude(1.);
        position2.setLongitude(1.);
        position2.setTime(new Date());
        position2.setServerTime(new Date());
        position2.setValid(true);
        position2.setValidStatus(Position.VALID_STATUS_CORRECT_POSITION);
        position2.setDevice(database.managerDevice);
        
        em.persist(position);
        em.persist(position2);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        
        Stream<Position> result = provider.getAllAvailablePositions(database.managerDevice, cal.getTime(), null, 100);
    
        assertEquals(1, result.count());
    }
    
    @Test
    public void getAllAvailable_maxZero() throws ProviderException {
        provider = new PositionProvider(em, database.manager, settings);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        Stream<Position> result = provider.getAllAvailablePositions(database.managed2Device, cal.getTime(), null, 0);
    
        assertEquals(3, result.count());
    }
    
    @Test
    public void getAllAvailable_maxOne() throws ProviderException {
        provider = new PositionProvider(em, database.manager, settings);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        Stream<Position> result = provider.getAllAvailablePositions(database.managed2Device, cal.getTime(), null, 1);
    
        assertEquals(1, result.count());
    }
    
    @Test
    public void getAllAvailable_withEndTime() throws ProviderException {
        provider = new PositionProvider(em, database.manager, settings);
        Calendar startCal = Calendar.getInstance();
        startCal.add(Calendar.DATE, -2);
        Calendar endCal = Calendar.getInstance();
        endCal.add(Calendar.MINUTE, -45);
        Stream<Position> result = provider.getAllAvailablePositions(database.managed2Device, startCal.getTime(), endCal.getTime(), 100);
    
        assertEquals(1, result.count());
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
