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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import pl.datamatica.traccar.api.Application;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.in.RegisterUserDto;
import pl.datamatica.traccar.api.dtos.out.UserDto;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.providers.UserProvider;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.model.User;
import spark.Request;
import spark.Spark;

public class UsersController extends ControllerBase {
    
    public static class Binder extends ControllerBinder{

        @Override
        public void bind() {      
            Spark.get(rootUrl(), (req, res) -> {
                UsersController uc = createController(req);
                return render(uc.get(), res);
            }, gson::toJson);

            Spark.get(rootUrl() + "/:id", (req, res) -> {
                UsersController uc = createController(req);
                return render(uc.get(Long.parseLong(req.params(":id"))), res);
            }, gson::toJson);
            
            Spark.post(rootUrl(), (req, res) ->{
                UsersController uc = createController(req);
                RegisterUserDto userDto = gson.fromJson(req.body(), RegisterUserDto.class);
                return render(uc.post(userDto), res);
            }, gson::toJson);
        }

        private UsersController createController(Request req) {
            RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
            UsersController uc = new UsersController(context);
            return uc;
        }

        @Override
        public String rootUrl() {
            return super.rootUrl() + "/users";
        }

    }
    
    private UserProvider up;
    
    public UsersController(RequestContext requestContext) {
        super(requestContext);
        up = requestContext.getUserProvider();
    }
    
    public HttpResponse get() throws Exception {
        List<UserDto> users = up.getAllAvailableUsers()
                .map(user -> new UserDto(user))
                .collect(Collectors.toList());
        return ok(users);
    }
    
    public HttpResponse get(long id) throws Exception {
        try {
            User other = up.getUser(id);
            return ok(new UserDto(other));
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse post(RegisterUserDto userDto) throws ProviderException {
        try {
            User user = up.createUser(userDto.getEmail(), userDto.getPassword(), userDto.getCheckMarketing());
            return created("/user/"+user.getId(), "");
        } catch (ProviderException ex) {
            switch(ex.getType()) {
                case ALREADY_EXISTS:
                    return conflict(MessageKeys.ERR_USER_ALREADY_EXISTS);
            }
            throw ex;
        }
    }
}
