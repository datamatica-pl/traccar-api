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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.out.ICachedDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.dtos.out.ListDto;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.responses.*;
import spark.Response;

public abstract class ControllerBase {
    
    protected RequestContext requestContext;
    
    public ControllerBase(RequestContext requestContext) {
        this.requestContext = requestContext;
    }
    
    protected HttpResponse handle(ProviderException e) throws ProviderException {
        switch(e.getType()) {
            case NOT_FOUND:
                return notFound();
            case ACCESS_DENIED:
                return forbidden();
        }
        throw e;
    }
    
    protected HttpResponse ok(Object result) {
        return new OkResponse(result);
    }
    
    protected HttpResponse okCached(ICachedDto content) {
        Date serverModification = content.getModificationTime();
        return okCached(content, serverModification);
    }
    
    protected<T extends ICachedDto> HttpResponse okCached(ListDto<T> content) {
        Date serverModification = content.getChanged().stream()
                .map(i -> i.getModificationTime())
                .max((d1, d2) -> d1.compareTo(d2))
                .orElse(Application.EMPTY_RESPONSE_MODIFICATION_DATE);
        return okCached(content, serverModification);
    }
    
    protected HttpResponse okCached(Object content, Date serverModification) {
        if(isModified(serverModification))
            return new OkCachedResponse(content, serverModification);
        return notModified(serverModification);
    }
    
    protected HttpResponse notModified(Date serverModification) {
        return new NotModifiedResponse(serverModification);
    }
    
    protected HttpResponse notFound() {
        ErrorDto error = new ErrorDto(MessageKeys.ERR_NOT_FOUND);
        return new ErrorResponse(HttpStatuses.NOT_FOUND, Collections.singletonList(error));
    }
    
    protected HttpResponse forbidden() {
        ErrorDto error = new ErrorDto(MessageKeys.ERR_ACCESS_DENIED);
        return new ErrorResponse(HttpStatuses.FORBIDDEN, Collections.singletonList(error));
    }
    
    protected HttpResponse badRequest(List<ErrorDto> errors) {
        return new ErrorResponse(HttpStatuses.BAD_REQUEST, errors);
    }
    
    protected HttpResponse badRequest(String... errorKeys) {
        List<ErrorDto> errors = Stream.of(errorKeys)
                .map(key -> new ErrorDto(key))
                .collect(Collectors.toList());
        return badRequest(errors);
    }
    
    protected HttpResponse conflict(String errorKey) {
        return new ErrorResponse(HttpStatuses.CONFLICT, 
                Collections.singletonList(new ErrorDto(errorKey)));
    }
    
    protected HttpResponse created(String route, Object resource) {
        return new CreatedResponse(route, resource);
    }
    
    protected boolean isModified(Date serverModification) {
        Date userModification = requestContext.getModificationDate();
        return userModification.getTime()/1000 < serverModification.getTime()/1000;
    }
    
    public static Object render(HttpResponse result, Response response) {
        return result.write(response);
    }
}
