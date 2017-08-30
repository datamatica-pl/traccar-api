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
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.PasswordHashMethod;
import pl.datamatica.traccar.model.User;

public class UserProviderTest {
    private UserProvider provider;
    private ApplicationSettings appSettings;
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
    public void testInit() throws ProviderException {
        em.getTransaction().begin();
        appSettings = new ApplicationSettings();
        provider = new UserProvider(em, appSettings);
        provider.authenticateUser(database.admin.getId());
    }
    
    @Test
    public void createUser_ok() throws ProviderException {
        String salt = "asdf";
        String password = "datamatica2016";
        String email = "newUser@test.pl";
        PasswordHashMethod passHash = PasswordHashMethod.MD5;
        String expectedPassword = passHash.doHash(password, salt);
        boolean marketing = false;
        
        appSettings.setSalt(salt);
        appSettings.setDefaultHashImplementation(PasswordHashMethod.MD5);
        User user = provider.registerUser(email, password, marketing);
        em.flush();
        em.refresh(user);
        
        assertEquals(email, user.getEmail());
        assertEquals(email, user.getLogin());
        assertEquals(expectedPassword, user.getPassword());
        assertEquals(marketing, user.getMarketingCheck());
        assertEquals(passHash, user.getPasswordHashMethod());
        assertTrue(user.getManager());
    }
    
    @Test
    public void createUser_alreadyExists() {
        appSettings.setSalt("asdf");
        appSettings.setDefaultHashImplementation(PasswordHashMethod.MD5);
        try {
            provider.registerUser("admin@admin.pl", "qwe85", true);
        } catch(ProviderException e) {
            assertEquals(Type.USER_ALREADY_EXISTS, e.getType());
            return;
        }
        fail();
    }
    
    @Test 
    public void removeUser_success() throws Exception {

        long removedId = database.managed2.getId();
        
        provider.authenticateUser(database.admin.getId());
        provider.removeUser(database.managed2.getId());

        em.flush();
        
        UserProvider provider2 = new UserProvider(em, appSettings);
        try {
            User user1 = provider2.getUser(removedId);
        } catch (ProviderException pe) {
            //success
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
