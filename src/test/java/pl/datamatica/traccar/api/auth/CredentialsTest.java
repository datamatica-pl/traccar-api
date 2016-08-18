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
import static org.junit.Assert.*;
import org.junit.Test;
import pl.datamatica.traccar.api.auth.AuthenticationException.ErrorType;

public class CredentialsTest {
    private final String login = "asdf@op.pl";
    private final String password = "asdf123";
    private final String base64Param = "YXNkZkBvcC5wbDphc2RmMTIz";
    
    @Test
    public void fromBasic_validParameter() {
        Credentials actual = Credentials.fromBasic(base64Param, StandardCharsets.UTF_8);
        assertEquals(login, actual.getLogin());
        assertEquals(password, actual.getPassword());
    }
    
    
    @Test
    public void fromBasic_parameterNotBase64() {
        try {
            Credentials.fromBasic("AlZa!(&/kXM", StandardCharsets.UTF_8);
        } catch(AuthenticationException e) {
            assertEquals(e.type, ErrorType.PARAMETER_NOT_BASE64);
            return;
        }
        fail();
    }
    
    @Test
    public void fromBasic_noColon() {
        String paramNoColon = "YXNkZkBvcC5wbA==";
        try {
            Credentials.fromBasic(paramNoColon, StandardCharsets.UTF_8);
        } catch(AuthenticationException e) {
            assertEquals(e.type, ErrorType.NO_COLON_IN_PARAMETER);
            return;
        }
        fail();
    }
    
    @Test
    public void toBasic() {
        Credentials credentials = new Credentials(login, password);
        assertEquals(base64Param, credentials.toBasic(StandardCharsets.UTF_8));
    }
}
