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

import com.google.gson.Gson;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.Context;
import pl.datamatica.traccar.api.TraccarConfig;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.in.EditRouteDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.dtos.out.RouteDto;
import pl.datamatica.traccar.api.exceptions.ConfigLoadException;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.providers.RouteProvider;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.model.Route;
import spark.Request;
import spark.Response;
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
            
            Spark.post(baseUrl()+"/findPolyline", (req, res) -> {
                RoutesController rc = createController(req);
                return rc.findPolyline(req, res);
            });
            
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
    
    private static final String CONFIG_API_KEY = "api.directions_key";
    private String apiKey;
    
    public RoutesController(RequestContext requestContext) {
        super(requestContext);
        try {
            apiKey = TraccarConfig.getInstance().getNotNullStringParam(CONFIG_API_KEY);
        } catch (ConfigLoadException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(RoutesController.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    
    private String findPolyline(Request req, Response res) throws Exception {
        final Gson gson = Context.getInstance().getGson();
        RouteProvider rp = requestContext.getRouteProvider();
        rp.updateLimit();
        if(rp.isLimitReached()) {
            res.status(429);
            return gson.toJson(Collections.singletonList(
                    new ErrorDto(MessageKeys.ERR_ROUTE_REQ_LIMIT_EXCEEDED)));
        }
        
        StringBuilder url = new StringBuilder("https://api.openrouteservice.org/directions?api_key=");
        url.append(apiKey);
        for(Map.Entry<String, String[]> param : req.queryMap().toMap().entrySet()) {
            url.append("&").append(param.getKey()).append("=").append(param.getValue()[0]);
        }
        HttpsURLConnection conn = null;
        try {
            conn = (HttpsURLConnection)new URL(url.toString()).openConnection();
            res.status(conn.getResponseCode());
            Scanner sc = new Scanner(conn.getInputStream()).useDelimiter("\\A");
            return sc.next();
        } finally {
            if(conn != null)
                conn.disconnect();
        }
    }
    
}
