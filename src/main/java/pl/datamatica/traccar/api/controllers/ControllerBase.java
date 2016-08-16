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
import java.util.List;
import pl.datamatica.traccar.api.responses.*;
import pl.datamatica.traccar.model.TimestampedEntity;
import spark.Response;

public abstract class ControllerBase<T extends TimestampedEntity> {
    protected RequestContext requestContext;
    
    public ControllerBase(RequestContext requestContext) {
        this.requestContext = requestContext;
    }
    
    protected IHttpResponse ok(List<T> list) {
        Date modificationTime = new Date(list.stream()
                .mapToLong(d -> d.getLastUpdate().getTime())
                .max()
                .orElse(1000));
        
        requestContext.setLastModified(modificationTime);
        if(!requestContext.isModified())
            return new NotModifiedResponse();
        
        return new OkResponse(list);
    }
    
    protected IHttpResponse ok(T item) {
        requestContext.setLastModified(item.getLastUpdate());
        if(!requestContext.isModified())
            return new NotModifiedResponse();
        return new OkResponse(item);
    }
    
    protected IHttpResponse notFound() {
        return new NotFoundResponse();
    }
    
    protected IHttpResponse forbidden() {
        return new ForbiddenResponse();
    }
    
    public static Object render(IHttpResponse result, Response response) {
        return result.write(response);
    }
}
