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
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.api.Application;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.dtos.in.AddGeoFenceDto;
import pl.datamatica.traccar.api.dtos.in.EditApplicationSettingsDto;
import pl.datamatica.traccar.api.dtos.out.ApplicationSettingsDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.providers.ApplicationSettingsProvider;
import pl.datamatica.traccar.api.providers.ProviderException;
import spark.Request;
import spark.Spark;

/**
 *
 * @author piotrkrzeszewski
 */
public class ApplicationSettingsController extends ControllerBase {
    
    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {

            Spark.get(baseUrl(), (req, res) -> {
                ApplicationSettingsController asc = createController(req);
                return render(asc.get(), res);
            }, gson::toJson);
            
            Spark.put(baseUrl(), (req, res) -> {
                ApplicationSettingsController asc = createController(req);
                EditApplicationSettingsDto easDto = gson.fromJson(req.body(), EditApplicationSettingsDto.class);
                return render(asc.put(easDto), res);
            }, gson::toJson);
        }

        private ApplicationSettingsController createController(Request req) {
            RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
            ApplicationSettingsController asc = new ApplicationSettingsController(context);
            return asc;
        }
        
        public String baseUrl() {
            return super.rootUrl() + "/applicationsettings";
        }
    }
    
    ApplicationSettingsProvider provider;
    RequestContext requestContext;
    
    public ApplicationSettingsController(RequestContext rc) {
        super(rc);
        provider = rc.getApplicationSettingsProvider();
        requestContext = rc;
    }
    
    public HttpResponse get() {
        if (!requestContext.getUser().getAdmin()) {
            return forbidden();
        }
        
        ApplicationSettingsDto as = new ApplicationSettingsDto.Builder().applicationSettings(provider.get()).build();
        return (HttpResponse)ok(as);
    }
    
    public HttpResponse put(EditApplicationSettingsDto updatedDto) {
        if (!requestContext.getUser().getAdmin()) {
            return forbidden();
        }
        
        List<ErrorDto> errors = EditApplicationSettingsDto.validate(updatedDto);
        if(!errors.isEmpty())
            return badRequest(errors);
        provider.updateApplicationSetting(updatedDto);
        return ok("");
    }
}
