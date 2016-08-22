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
import java.util.List;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.dtos.out.ICachedDto;
import pl.datamatica.traccar.api.utils.DateUtil;

public class OkCachedResponse extends OkResponse {
    
    private static final String LAST_MODIFIED_HEADER = "Last-Modified";
    
    private final Date userModification;
    private final Date serverModification;
    
    public OkCachedResponse(ICachedDto item, Date userVersion) {
        super(item);
        this.userModification = userVersion;
        this.serverModification = item.getModificationTime();
    }
    
    public <T extends ICachedDto> OkCachedResponse(List<T> items, Date userVersion) {
        super(items);
        this.userModification = userVersion;
        this.serverModification = items.stream()
                .map(i -> i.getModificationTime())
                .max((d1, d2) -> d1.compareTo(d2))
                .orElse(Application.EMPTY_RESPONSE_MODIFICATION_DATE);
    }

    @Override
    public Object getContent() {
        if(isModified())
            return "";
        return super.getContent();
    }

    @Override
    public Iterable getHeaders() {
        return Collections.singleton(new HttpHeader(LAST_MODIFIED_HEADER, DateUtil.formatDate(serverModification)));
    }
   
    @Override
    public int getHttpStatus() {
        if(isModified())
            return HttpStatuses.NOT_MODIFIED;
        return super.getHttpStatus();
    }
    
    private boolean isModified() {
        return userModification.compareTo(serverModification) >= 0;
    }
}
