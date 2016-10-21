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

import java.util.List;
import java.util.stream.Collectors;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.dtos.in.AddGeoFenceDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.dtos.out.GeoFenceDto;
import pl.datamatica.traccar.api.providers.GeoFenceProvider;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.model.GeoFence;
import spark.Request;
import spark.Spark;

public class GeofencesController extends ControllerBase{
    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {
            Spark.get(rootUrl(), (req, res) -> {
                GeofencesController controller = createController(req);
                return render(controller.get(), res);
            }, gson::toJson);
            
            Spark.get(rootUrl() + "/:id", (req, res) -> {
                GeofencesController controller = createController(req);
                return render(controller.get(Long.parseLong(req.params(":id"))), res);
            }, gson::toJson);
            
            Spark.post(rootUrl(), (req, res) -> {
                GeofencesController controller = createController(req);
                AddGeoFenceDto geoFenceDto = gson.fromJson(req.body(), AddGeoFenceDto.class);
                return render(controller.post(geoFenceDto), res);
            },gson::toJson);
            
            Spark.put(rootUrl()+"/:id", (req, res) -> {
                GeofencesController controller = createController(req);
                AddGeoFenceDto geoFenceDto = gson.fromJson(req.body(), AddGeoFenceDto.class);
                return render(controller.put(Long.parseLong(req.params(":id")), geoFenceDto), res);
            }, gson::toJson);
            
            Spark.delete(rootUrl()+"/:id", (req, res) -> {
                GeofencesController controller = createController(req);
                return render(controller.delete(Long.parseLong(req.params(":id"))), res);
            }, gson::toJson);
        }
        
        @Override
        public String rootUrl() {
            return super.rootUrl() + "/geofences";
        }
        
        private GeofencesController createController(Request req) {
            RequestContext rc = req.attribute(Application.REQUEST_CONTEXT_KEY);
            return new GeofencesController(rc);
        }
    }
    
    GeoFenceProvider provider;
    
    public GeofencesController(RequestContext rc) {
        super(rc);
        provider = rc.getGeoFencesProvider();
    }
    
    public HttpResponse get() {
        List<GeoFenceDto> gfs = provider.getAllAvailableGeoFences()
                .map(gf -> new GeoFenceDto.Builder().geoFence(gf).build())
                .collect(Collectors.toList());
        return ok(gfs);
    }
    
    public HttpResponse get(long id) throws ProviderException {
        try {
            GeoFenceDto gf = new GeoFenceDto.Builder().geoFence(provider.getGeoFence(id)).build();
            return okCached(gf);
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse post(AddGeoFenceDto geoFenceDto) {
        List<ErrorDto> errors = AddGeoFenceDto.validate(geoFenceDto);
        if(!errors.isEmpty())
            return badRequest(errors);
        
        GeoFence gf = provider.createGeoFence(geoFenceDto);
        
        return created("geofences/"+gf.getId(), new GeoFenceDto.Builder().geoFence(gf).build());
    }
    
    public HttpResponse put(long id, AddGeoFenceDto geoFenceDto) throws ProviderException {
        List<ErrorDto> errors = AddGeoFenceDto.validate(geoFenceDto);
        if(!errors.isEmpty())
            return badRequest(errors);
        try {
            provider.updateGeoFence(id, geoFenceDto);
            return ok("");
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse delete(long id) throws ProviderException {
        try {
            provider.removeGeoFence(id);
            return ok("");
        } catch(ProviderException e) {
            return handle(e);
        }
    }
}
