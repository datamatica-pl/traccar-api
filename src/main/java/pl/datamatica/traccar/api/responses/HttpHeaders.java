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
package pl.datamatica.traccar.api.responses;
import org.apache.commons.codec.binary.Base64;

public class HttpHeaders {

    public static final String LAST_MODIFIED = "Last-Modified";
    public static final String LOCATION = "Location";

    public static final String getEncodedCredentials(String username, String password) {
        String userPassword = username + ":" + password;
        return Base64.encodeBase64String(userPassword.getBytes());
    }
}
