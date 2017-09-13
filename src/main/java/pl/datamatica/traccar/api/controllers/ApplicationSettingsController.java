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
import pl.datamatica.traccar.api.Context;
import pl.datamatica.traccar.api.EventDaemon;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.dtos.in.EditApplicationSettingsDto;
import pl.datamatica.traccar.api.dtos.out.ApplicationSettingsDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.providers.ApplicationSettingsProvider;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.model.UserPermission;
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
    
    public ApplicationSettingsController(RequestContext rc) {
        super(rc);
        provider = rc.getApplicationSettingsProvider();
        provider.setUserGroupsProvider(rc.getUserGroupProvider());
        provider.setRequestUser(rc.getUser());
        requestContext = rc;
    }
    
    public HttpResponse get() {
        ApplicationSettingsDto.Builder builder = new ApplicationSettingsDto.Builder().applicationSettings(provider.get());
        
        if (!requestContext.getUser().hasPermission(UserPermission.SERVER_MANAGEMENT)) {
            // Some fields should be passed only to admin
            builder = builder.purgeConfidentialData();
        }
        ApplicationSettingsDto as = builder.build();
        return (HttpResponse)ok(as);
    }
    
    public HttpResponse put(EditApplicationSettingsDto updatedDto) throws ProviderException {
        List<ErrorDto> errors = EditApplicationSettingsDto.validate(updatedDto);
        if(!errors.isEmpty())
            return badRequest(errors);
        try {
            boolean wasRecEnabled = provider.get().isEventRecordingEnabled();
            provider.updateApplicationSetting(updatedDto);
            if(wasRecEnabled && !provider.get().isEventRecordingEnabled())
                EventDaemon.getInstance().stop();
            else if(!wasRecEnabled && provider.get().isEventRecordingEnabled())
                EventDaemon.getInstance().start();
            return ok("");
        } catch (ProviderException e) {
            return handle(e);
        }
    }
}
