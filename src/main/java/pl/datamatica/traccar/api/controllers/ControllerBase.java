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
import javax.persistence.EntityManager;
import pl.datamatica.traccar.api.responses.*;
import pl.datamatica.traccar.model.TimestampedEntity;
import pl.datamatica.traccar.model.User;
import spark.Response;

public abstract class ControllerBase {
    
    public static String rootUrl() {
        return "v1";
    }
    
    protected RequestContext requestContext;
    
    public ControllerBase(RequestContext requestContext) {
        this.requestContext = requestContext;
    }
    
    protected EntityManager entityManager() {
        return requestContext.getEntityManager();
    }
    
    protected User requestUser() {
        return requestContext.getUser();
    }
    
    protected IHttpResponse ok(Object result) {
        return new OkResponse(result);
    }
    
    protected IHttpResponse ok(List<TimestampedEntity> list) {
        Date modificationTime = new Date(list.stream()
                .mapToLong(d -> d.getLastUpdate().getTime())
                .max()
                .orElse(1000));
        
        requestContext.setLastModified(modificationTime);
        if(!requestContext.isModified())
            return new NotModifiedResponse();
        
        return new OkResponse(list);
    }
    
    protected IHttpResponse ok(TimestampedEntity item) {
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
    
    protected IHttpResponse badRequest() {
        return new BadRequestResponse();
    }
    
    protected IHttpResponse created(String route) {
        return new CreatedResponse(route);
    }
    
    public static Object render(IHttpResponse result, Response response) {
        return result.write(response);
    }
}
