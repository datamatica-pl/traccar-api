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

public class AuthenticationException extends IllegalArgumentException {

    public static enum ErrorType {
        HEADER_FORMAT("Too many words in authentication header"),
        INVALID_SCHEME("Invalid scheme name"),
        PARAMETER_NOT_BASE64("Authentication parameter not in base 64"),
        NO_PASSWORD("Password not found in parameter"),
        NO_CREDENTIALS("No credentials were included in request"),
        NO_SUCH_USER("Invalid username or password");
        
        private String msg;
        ErrorType(String msg) {
            this.msg = msg;
        }

        public String getMessage() {
            return msg;
        }
        
    }
    
    ErrorType type;
    
    public AuthenticationException(ErrorType type) {
        super();
        this.type = type;
    }

    public AuthenticationException(ErrorType t, Throwable cause) {
        super(cause);
        this.type = t;
    }

    @Override
    public String getMessage() {
        return type.getMessage();
    }
    
}
