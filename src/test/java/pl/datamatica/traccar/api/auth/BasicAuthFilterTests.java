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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import pl.datamatica.traccar.model.User;
import static org.junit.Assert.*;

public class BasicAuthFilterTests {
    
    private Credentials validCredentials;
    private Credentials invalidCredentials;
    private User validUser;
    private BasicAuthFilter filter;
    
    public BasicAuthFilterTests() {
        validCredentials = new Credentials("asdf@op.pl", "asdf123");
        invalidCredentials = new Credentials("qwe@test.pl","test");
        
        validUser = new User();
        validUser.setLogin("top-secret-login");
    }
    
    @Before
    public void testInit() {
        filter = new BasicAuthFilter("", new IPasswordValidator() {
            @Override
            public User getUser(Credentials credentials) {
                return validCredentials.equals(credentials) ? validUser : null;
            }  
        });
    }
    
    @Test
    public void validCredentials() {
        User actual = filter.verifyCredentials(validCredentials);
        assertEquals(validUser, actual);
    }
    
    @Test
    public void noSuchUser() {
        try {
            filter.verifyCredentials(invalidCredentials);
        } catch(AuthenticationException e) {
            assertEquals(e.type, AuthenticationException.ErrorType.NO_SUCH_USER);
            return;
        }
        fail();
    }
    
}
