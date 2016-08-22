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

import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import pl.datamatica.traccar.api.dtos.out.ICachedDto;
import pl.datamatica.traccar.api.responses.*;
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
    
    protected HttpResponse ok(Object result) {
        return new OkResponse(result);
    }
    
    protected HttpResponse okCached(ICachedDto content) {
        return new OkCachedResponse(content, requestContext.getModificationDate());
    }
    
    protected<T extends ICachedDto> HttpResponse okCached(List<T> content) {
        return new OkCachedResponse(content, requestContext.getModificationDate());
    }
    
    protected HttpResponse notFound() {
        return new ErrorResponse(HttpStatuses.NOT_FOUND, Collections.emptyList());
    }
    
    protected HttpResponse forbidden() {
        return new ErrorResponse(HttpStatuses.FORBIDDEN, Collections.emptyList());
    }
    
    protected HttpResponse badRequest() {
        return new ErrorResponse(HttpStatuses.BAD_REQUEST, Collections.emptyList());
    }
    
    protected HttpResponse created(String route, Object resource) {
        return new CreatedResponse(route, resource);
    }
    
    public static Object render(HttpResponse result, Response response) {
        return result.write(response);
    }
}
