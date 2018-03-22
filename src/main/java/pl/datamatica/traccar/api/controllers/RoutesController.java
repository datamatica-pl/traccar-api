/*
 *  Copyright (C) 2018  Datamatica (dev@datamatica.pl)
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *  
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *  
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pl.datamatica.traccar.api.controllers;

import java.util.List;
import java.util.stream.Collectors;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.dtos.in.EditRouteDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.dtos.out.RouteDto;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.model.Route;
import spark.Request;
import spark.Spark;

/**
 *
 * @author ŁŁ
 */
public class RoutesController extends ControllerBase {
    
    public static class Binder extends ControllerBinder {
        @Override
        public void bind() {
            Spark.get(baseUrl(), (req, res) -> {
                RoutesController rc = createController(req);
                boolean archived = "true".equals(req.queryParams("archived"));
                return render(rc.get(archived), res);
            }, gson::toJson);
            
            Spark.put(baseUrl()+"/:id", (req, res) -> {
                RoutesController rc = createController(req);
                long id = Long.parseLong(req.params(":id"));
                EditRouteDto dto = gson.fromJson(req.body(), EditRouteDto.class);
                return render(rc.put(id, dto), res);
            }, gson::toJson);
            
            Spark.post(baseUrl(), (req, res) -> {
                RoutesController rc = createController(req);
                EditRouteDto dto = gson.fromJson(req.body(), EditRouteDto.class);
                return render(rc.post(dto), res);
            }, gson::toJson);
            
            Spark.delete(baseUrl()+"/:id", (req, res) -> {
                RoutesController rc = createController(req);
                long id = Long.parseLong(req.params(":id"));
                return render(rc.delete(id), res);
            }, gson::toJson);
        }

        private RoutesController createController(Request req) {
            return new RoutesController(req.attribute(Application.REQUEST_CONTEXT_KEY));
        }
        
        public String baseUrl() {
            return rootUrl()+"/routes";
        }
    }
    
    public RoutesController(RequestContext requestContext) {
        super(requestContext);
    }
    
    public HttpResponse<List<RouteDto>> get(boolean archive) throws ProviderException {
        List<RouteDto> dtos = requestContext.getRouteProvider().getAllAvailableRoutes(archive)
                .map(r -> new RouteDto.Builder().route(r).build()).collect(Collectors.toList());
        return ok(dtos);
    }
    
    public HttpResponse put(long id, EditRouteDto dto) throws ProviderException {
        List<ErrorDto> errors = EditRouteDto.validate(dto);
        if(!errors.isEmpty())
            return badRequest(errors);
        
        try {
            Route r = requestContext.getRouteProvider().updateRoute(id, dto);
            return ok(new RouteDto.Builder().route(r).build());
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse post(EditRouteDto dto) throws ProviderException {
        List<ErrorDto> errors = EditRouteDto.validate(dto);
        if(!errors.isEmpty())
            return badRequest(errors);
        
        try {
            Route r = requestContext.getRouteProvider().createRoute(dto);
            return created("routes/"+r.getId(), new RouteDto.Builder().route(r).build());
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse delete(long id) throws ProviderException {
        try {
            requestContext.getRouteProvider().deleteRoute(id);
            return ok("");
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
}
