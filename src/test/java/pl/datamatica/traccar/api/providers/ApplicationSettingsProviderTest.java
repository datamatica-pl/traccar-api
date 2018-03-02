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
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.datamatica.traccar.api.dtos.in.EditApplicationSettingsDto;
import pl.datamatica.traccar.api.dtos.out.ApplicationSettingsDto;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.User;

/**
 *
 * @author piotrkrzeszewski
 */
public class ApplicationSettingsProviderTest {
    
    private ApplicationSettingsProvider provider;
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
    public void update_defaultGroup() throws ProviderException {
        prepareProvider(database.admin);
        
        ApplicationSettings as = provider.get();
        ApplicationSettingsDto dto = new ApplicationSettingsDto.Builder().
                                        applicationSettings(as).
                                        defaultUserGroupId(database.adminsGroup.getId()).
                                        build();
        EditApplicationSettingsDto editDto = (EditApplicationSettingsDto)dto;
        
        provider.updateApplicationSetting(editDto);
        em.flush();
        
        ApplicationSettings updated = provider.get();
        assertEquals(database.adminsGroup.getId(), updated.getDefaultGroup().getId());
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
        UserGroupProvider ugp = new UserGroupProvider(em, requestUser);
        provider = new ApplicationSettingsProvider(em);
        ugp.setApplicationSettingsProvider(provider);
        UserProvider up = new UserProvider(em, provider.get(), null);
        up.authenticateUser(requestUser.getId());
        ugp.setUserProvider(up);
        provider.setUserGroupsProvider(ugp);
        provider.setRequestUser(requestUser);
    }
}
