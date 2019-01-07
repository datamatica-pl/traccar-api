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
import java.util.Date;
import pl.datamatica.traccar.api.utils.DateUtil;

public class OkCachedResponse extends OkResponse {
    private final Date serverModification;
    
    public OkCachedResponse(Object item, Date serverModification) {
        super(item);
        this.serverModification = serverModification;
    }

    @Override
    public Iterable getHeaders() {
        if(serverModification != null)
            return Collections.singleton(new HttpHeader(HttpHeaders.LAST_MODIFIED, 
                    DateUtil.formatDateForResponse(serverModification)));
        return Collections.EMPTY_LIST;
    }
}
