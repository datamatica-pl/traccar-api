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

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Objects;
import pl.datamatica.traccar.api.dtos.MessageKeys;

public class Credentials {
    private static final String BASIC_AUTH_SEPARATOR = ":";
    
    private final String login;
    private final String password;
    
    public Credentials(String login, String password) {
        this.login = login;
        this.password = password;
    }
    
    public String getLogin() {
        return login;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String toBasic(Charset charset) {
        String parameter = login + BASIC_AUTH_SEPARATOR + password;
        return Base64.getEncoder().encodeToString(parameter.getBytes(charset));
    }
    
    public static Credentials fromBasic(String credentials, Charset charset) {
        String decoded = null;
        try {
            decoded = new String(Base64.getDecoder().decode(credentials), charset);
        } catch(IllegalArgumentException e) {
            throw new AuthenticationException(MessageKeys.ERR_AUTH_PARAMETER_NOT_BASE64);
        }
        int firstSeparator = decoded.indexOf(BASIC_AUTH_SEPARATOR);
        if(firstSeparator == -1)
            throw new AuthenticationException(MessageKeys.ERR_AUTH_NO_COLON_IN_PARAMETER);
        String login = decoded.substring(0, firstSeparator);
        String password = decoded.substring(firstSeparator + 1);
        return new Credentials(login, password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.login, this.password);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Credentials other = (Credentials) obj;
        
        return Objects.equals(this.login, other.login) 
                && Objects.equals(this.password, other.password);
    }
    
    
}
