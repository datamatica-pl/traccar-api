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

import java.util.Collections;
import spark.Response;

public abstract class HttpResponse<T>{
    public abstract int getHttpStatus();
    public abstract T getContent();
    public Iterable<HttpHeader> getHeaders() {
        return Collections.EMPTY_LIST;
    }

    public final Object write(Response response) {
        response.status(getHttpStatus());
        response.type("application/json; charset=utf-8");
        response.header("Content-Encoding", "gzip");
        for(HttpHeader header : getHeaders())
            response.header(header.getName(), header.getValue());
        return getContent();
    }
}
