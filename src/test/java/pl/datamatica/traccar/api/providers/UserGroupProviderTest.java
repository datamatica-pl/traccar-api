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
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.datamatica.traccar.api.dtos.in.AddUserGroupDto;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserGroup;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author piotrkrzeszewski
 */
public class UserGroupProviderTest {
    
    private UserGroupProvider provider;
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
    public void getAll_admin() throws ProviderException {
        prepareProvider(database.admin);
        
        List<UserGroup> result = provider.getAllAvailableGroups().collect(Collectors.toList());
        
        assertEquals(2, result.size());
    }
    
    @Test
    public void getAll_nonAdmin() throws ProviderException {
        prepareProvider(database.manager);
        
        List<UserGroup> result = provider.getAllAvailableGroups().collect(Collectors.toList());
        
        assertEquals(1, result.size());
    }
    
    @Test
    public void update_sameName() throws ProviderException {
        prepareProvider(database.admin);
        
        AddUserGroupDto dto = new AddUserGroupDto("users", Collections.singleton(UserPermission.USER_GROUP_MANAGEMENT));
        
        try {
            provider.updateUserGroup(database.adminsGroup.getId(), dto);
        } catch(ProviderException e) {
            if (e.getType() != ProviderException.Type.GROUP_ALREADY_EXISTS)
                fail();
            return;     
        }
        fail();
    }
    
    @Test
    public void delete_success() throws ProviderException {
        prepareProvider(database.admin);
        
        provider.deleteUserGroup(database.adminsGroup.getId());
        em.flush();
        
        List<UserGroup> result = provider.getAllAvailableGroups().collect(Collectors.toList());

        assertEquals(1, result.size());
    }
    
    @Test
    public void delete_defaultGroup() throws ProviderException {
        prepareProvider(database.admin);
        
        try {
            provider.deleteUserGroup(database.usersGroup.getId());
        } catch(ProviderException e) {
            if (e.getType() != ProviderException.Type.DELETING_DEFAULT)
                fail();
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
    
    private void prepareProvider(User requestUser) throws ProviderException {
        provider = new UserGroupProvider(em, requestUser);
        ApplicationSettingsProvider asp = new ApplicationSettingsProvider(em);
        asp.setRequestUser(requestUser);
        asp.setUserGroupsProvider(provider);
        provider.setApplicationSettingsProvider(asp);
        UserProvider up = new UserProvider(em, asp.get());
        up.authenticateUser(requestUser.getId());
        provider.setUserProvider(up);
    }
}
