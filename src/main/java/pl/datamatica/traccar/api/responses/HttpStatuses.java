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

public class HttpStatuses {
    public static int OK = 200;
    public static int CREATED = 201;
    
    public static int NOT_MODIFIED = 304;
    
    public static int BAD_REQUEST = 400;
    public static int FORBIDDEN = 403;
    public static int NOT_FOUND = 404;
    public static int NOT_ACCEPTABLE = 406;
    public static int TIMED_OUT = 408;
    public static int CONFLICT = 409;
}
