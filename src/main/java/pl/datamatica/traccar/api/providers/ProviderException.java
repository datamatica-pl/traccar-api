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

public class ProviderException extends Exception {
    public enum Type {
        NOT_FOUND, ACCESS_DENIED, ALREADY_DELETED,
        ALREADY_EXISTS, INVALID_IMEI
    }
    
    private final Type type;
    
    public ProviderException(Type type) {
        this.type = type;
    }
    
    public Type getType() {
        return type;
    }
}
