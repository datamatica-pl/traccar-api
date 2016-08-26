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
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.in.NotificationTokenDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.dtos.out.UserDto;
import pl.datamatica.traccar.api.responses.HttpResponse;
import spark.Spark;
import pl.datamatica.traccar.api.validators.INotificationTokenValidator;
import spark.Request;

public class SessionController extends ControllerBase {
    
    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {
            Spark.get(rootUrl()+"/user", (req, res) -> {
                SessionController controller = createController(req);
                return render(controller.getUser(), res);
            }, gson::toJson);
            
            Spark.get(rootUrl()+"/notificationtoken", (req, res) -> {
                SessionController controller = createController(req);
                NotificationTokenDto tokenDto = gson.fromJson(req.body(), NotificationTokenDto.class);
                return render(controller.putNotificationToken(tokenDto), res);
            }, gson::toJson);
            
            Spark.delete(rootUrl(), (req, res) -> {
                SessionController controller = createController(req);
                return render(controller.delete(), res);
            }, gson::toJson);
        }

        @Override
        public String rootUrl() {
            return super.rootUrl() + "/session";
        }    
        
        private SessionController createController(Request req) {
            RequestContext rc = req.attribute(Application.REQUEST_CONTEXT_KEY);
            return new SessionController(rc, t -> t.length() >= 10);
        }
    }
    
    private INotificationTokenValidator validator;
    
    public SessionController(RequestContext requestContext, INotificationTokenValidator validator) {
        super(requestContext);
        this.validator = validator;
    }
    
    public HttpResponse getUser() {
        return ok(new UserDto.Builder().user(requestContext.getUser()).build());
    }
    
    public HttpResponse putNotificationToken(NotificationTokenDto tokenDto) {
        List<ErrorDto> validationErrors = NotificationTokenDto.validate(tokenDto);
        if(!validationErrors.isEmpty())
            return badRequest(validationErrors);
        String token = tokenDto.getToken();
        if(validator.isValid(token)) {
            requestContext.session().attribute(Application.NOTIFICATION_TOKEN_SESSION_KEY, token);
            return ok("");
        }
        return badRequest(MessageKeys.ERR_TOKEN_REJECTED);
    }
    
    public HttpResponse delete() {
        requestContext.session().invalidate();
        return ok("");
    }  
}
