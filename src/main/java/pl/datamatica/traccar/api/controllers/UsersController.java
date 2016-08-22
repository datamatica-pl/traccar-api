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

import com.google.gson.Gson;
import java.util.List;
import java.util.stream.Collectors;
import pl.datamatica.traccar.api.Context;
import pl.datamatica.traccar.api.dtos.out.UserDto;
import pl.datamatica.traccar.api.providers.UserProvider;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.api.transformers.UserTransformer;
import pl.datamatica.traccar.model.User;
import spark.Spark;

public class UsersController extends ControllerBase { 
    
    private UserProvider up;
    
    public UsersController(RequestContext requestContext) {
        super(requestContext);
        up = new UserProvider(entityManager());
    }
    
    public HttpResponse get() throws Exception {
        List<UserDto> users = up.getAllAvailableUsers(requestUser())
                .map(user -> new UserDto(user))
                .collect(Collectors.toList());
        return ok(users);
    }
    
    public HttpResponse get(long id) throws Exception {
        User other = up.getUser(id);
        if(other == null)
            return notFound();
        if(UserProvider.isVisibleToUser(other, requestUser()))
            return ok(new UserDto(other));
        else
            return forbidden();
    }
    
    public static void registerMethods() {
        Gson gson = Context.getInstance().getGson();
        UserTransformer userTransformer = new UserTransformer(gson);
        
        Spark.get(rootUrl(), (req, res) -> {
            RequestContext context = new RequestContext(req, res);
            UsersController uc = new UsersController(context);
            return render(uc.get(), res);
        }, gson::toJson);
        
        Spark.get(rootUrl() + "/:id", (req, res) -> {
            RequestContext context = new RequestContext(req, res);
            UsersController uc = new UsersController(context);
            return render(uc.get(Long.parseLong(req.params(":id"))), res);
        }, gson::toJson);
    }
    
    public static String rootUrl() {
        return ControllerBase.rootUrl() + "/users";
    }
}