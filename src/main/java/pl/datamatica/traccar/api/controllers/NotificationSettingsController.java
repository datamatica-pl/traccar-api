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
import pl.datamatica.traccar.api.Application;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.dtos.out.NotificationSettingsDto;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.model.UserPermission;
import spark.Request;
import spark.Spark;

public class NotificationSettingsController extends ControllerBase {
    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {
            Spark.get(rootUrl(), (req, res) -> {
                NotificationSettingsController nsc = createController(req);
                return render(nsc.get(), res);
            }, gson::toJson);
            
            Spark.put(rootUrl(), (req, res) -> { 
                NotificationSettingsController nsc = createController(req);
                NotificationSettingsDto dto = gson.fromJson(req.body(), NotificationSettingsDto.class);
                return render(nsc.put(dto), res);
            }, gson::toJson);
        }

        private NotificationSettingsController createController(Request req) throws Exception {
            RequestContext rc = req.attribute(Application.REQUEST_CONTEXT_KEY);
            NotificationSettingsController nsc = new NotificationSettingsController(rc);
            return nsc;
        }
        
        @Override
        public String rootUrl() {
            return super.rootUrl() + "/notificationsettings";
        }
        
    }
    
    public NotificationSettingsController(RequestContext requestContext) {
        super(requestContext);
    }
    
    public HttpResponse get() throws ProviderException {
        
        if (!requestContext.getUser().hasPermission(UserPermission.NOTIFICATIONS))
            return handle(new ProviderException(ProviderException.Type.ACCESS_DENIED));
        
        return ok(new NotificationSettingsDto(requestContext.getUser().getMobileNotificationSettings()));
    }
    
    public HttpResponse put(NotificationSettingsDto dto) throws ProviderException {
        List<ErrorDto> validationErrors = NotificationSettingsDto.validate(dto);
        if(!validationErrors.isEmpty())
            return badRequest(validationErrors);
        try {
            requestContext.getNotificationSettingsProvider().updateNotificationSettings(dto);
            return ok("");
        } catch(ProviderException e) {
            return handle(e);
        }
    }
}
