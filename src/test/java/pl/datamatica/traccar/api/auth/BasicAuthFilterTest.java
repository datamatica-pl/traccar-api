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
package pl.datamatica.traccar.api.auth;

import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;
import pl.datamatica.traccar.model.User;
import static org.junit.Assert.*;
import org.mockito.Mockito;
import static pl.datamatica.traccar.api.auth.AuthenticationException.*;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.providers.UserProvider;

public class BasicAuthFilterTest {
    
    private final Credentials validCredentials;
    private final Credentials invalidCredentials;
    private final User validUser;
    private BasicAuthFilter filter;
    private UserProvider users;
    
    public BasicAuthFilterTest() {       
        validCredentials = new Credentials("asdf@op.pl", "asdf123");
        invalidCredentials = new Credentials("qwe@test.pl","test");
        
        validUser = new User();
        validUser.setLogin("top-secret-login");
        
        users = Mockito.mock(UserProvider.class);
        Mockito.when(users.authenticateUser(validCredentials.getLogin(), validCredentials.getPassword()))
                .thenReturn(validUser);
        Mockito.when(users.authenticateUser(invalidCredentials.getLogin(), invalidCredentials.getPassword()))
                .thenThrow(new AuthenticationException(MessageKeys.ERR_AUTH_NO_SUCH_USER));
    }
    
    @Before
    public void testInit() {
        filter = new BasicAuthFilter();
    }
    
    @Test
    public void verifyCredentials_validCredentials() {
        User actual = filter.verifyCredentials(validCredentials, users);
        assertEquals(validUser, actual);
    }
    
    @Test
    public void verifyCredentials_noSuchUser() {
        try {
            filter.verifyCredentials(invalidCredentials, users);
        } catch(AuthenticationException e) {
            assertEquals(e.getMessage(), MessageKeys.ERR_AUTH_NO_SUCH_USER);
            return;
        }
        fail();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void verifyCredentials_noCredentials() {
        filter.verifyCredentials(null, users);
    }
    
    @Test
    public void readCredentials_noCredentials() {
        try {
            filter.readCredentials(null);
        } catch(AuthenticationException e) {
            assertEquals(e.getMessage(), MessageKeys.ERR_AUTH_NO_CREDENTIALS);
            return;
        }
        fail();
    }
    
    @Test
    public void readCredentials_invalidScheme() {
        try{
            filter.readCredentials("NotBasic "+ getValidHeaderParam());
        } catch(AuthenticationException e) {
            assertEquals(e.getMessage(), MessageKeys.ERR_AUTH_INVALID_SCHEME);
        }
    }
    
    @Test
    public void readCredentials_scheme_ignoreCase() {
        Credentials actual = filter.readCredentials("bAsIC " + getValidHeaderParam());
        assertEquals(validCredentials, actual);
    }
    
    @Test
    public void readCredentials_tooManyWords() {
        try {
            filter.readCredentials("Basic "+getValidHeaderParam()+" asdfasdfasdf");
        } catch(AuthenticationException e) {
            assertEquals(e.getMessage(), MessageKeys.ERR_AUTH_INVALID_HEADER_FORMAT);
            return;
        }
        fail();
    }
    
    @Test
    public void readCredentials_tooFewWords() {
        try {
            filter.readCredentials("Basic");
        } catch(AuthenticationException e) {
            assertEquals(e.getMessage(), MessageKeys.ERR_AUTH_INVALID_HEADER_FORMAT);
            return;
        }
        fail();
    }
    
    @Test
    public void readCredentials_emptyAuthParameter() {
        try {
            filter.readCredentials("Basic ");
        } catch(AuthenticationException e) {
            assertEquals(e.getMessage(), MessageKeys.ERR_AUTH_INVALID_HEADER_FORMAT);
            return;
        }
        fail();
    }
    
    private String getValidHeaderParam() {
        return validCredentials.toBasic(StandardCharsets.UTF_8);
    }
}
