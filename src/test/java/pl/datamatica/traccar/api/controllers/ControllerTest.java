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
package pl.datamatica.traccar.api.controllers;

import java.util.Date;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import pl.datamatica.traccar.api.responses.HttpHeader;
import pl.datamatica.traccar.api.responses.HttpHeaders;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.api.utils.DateUtil;

public class ControllerTest {
    public static HttpHeader lastModifiedHeader(Date date) {
        return new HttpHeader(HttpHeaders.LAST_MODIFIED, 
                DateUtil.formatDate(date));
    }

    public static Stream getHeaderStream(HttpResponse response) {
        return StreamSupport.stream(response.getHeaders().spliterator(), false);
    }
}
