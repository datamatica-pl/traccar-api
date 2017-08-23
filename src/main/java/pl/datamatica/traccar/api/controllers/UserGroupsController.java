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

import com.google.gson.reflect.TypeToken;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import pl.datamatica.traccar.api.Application;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.in.AddUserGroupDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.dtos.out.UserGroupDto;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.providers.UserGroupProvider;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.model.UserGroup;
import spark.Request;
import spark.Spark;

/**
 *
 * @author piotrkrzeszewski
 */
public class UserGroupsController extends ControllerBase {
    
    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {

            Spark.get(baseUrl(), (req, res) -> {
                UserGroupsController ugc = createController(req);
                return render(ugc.get(), res);
            }, gson::toJson);
            
            Spark.get(baseUrl()+"/:id", (req, res) -> {
                UserGroupsController ugc = createController(req);
                return render(ugc.get(Long.parseLong(req.params(":id"))), res);
            }, gson::toJson);
            
            Spark.post(baseUrl(), (req, res) -> {
                UserGroupsController ugc = createController(req);
                AddUserGroupDto userGroupDto = gson.fromJson(req.body(), AddUserGroupDto.class);
                return render(ugc.post(userGroupDto), res);
            }, gson::toJson);
            
            Spark.put(baseUrl()+"/:id", (req, res) -> {
                UserGroupsController ugc = createController(req);
                AddUserGroupDto dto = gson.fromJson(req.body(), AddUserGroupDto.class);
                return render(ugc.put(Long.parseLong(req.params(":id")), dto), res);
            }, gson::toJson);
            
            Spark.delete(baseUrl()+"/:id", (req, res) -> {
                UserGroupsController ugc = createController(req);
                return render(ugc.delete(Long.parseLong(req.params(":id"))), res);
            }, gson::toJson);

            Spark.get(baseUrl()+"/:id/users", (req, res) -> {
                UserGroupsController ugc = createController(req);
                return render(ugc.getAllUsers(Long.parseLong(req.params(":id"))), res);
            }, gson::toJson);
            
            Spark.put(baseUrl()+"/:id/users", (req, res) -> {
                UserGroupsController ugc = createController(req);
                long id = Long.parseLong(req.params(":id"));
                Set<Long> uids = gson.fromJson(req.body(), 
                        new TypeToken<Set<Long>>() {}.getType());
                return render(ugc.updateUsers(id, uids), res);
            }, gson::toJson);
        }

        private UserGroupsController createController(Request req) {
            RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
            UserGroupsController ugc = new UserGroupsController(context);
            return ugc;
        }
        
        public String baseUrl() {
            return super.rootUrl() + "/usergroups";
        }
    }
    
    UserGroupProvider provider;
    
    public UserGroupsController(RequestContext rc) {
        super(rc);
        provider = rc.getUserGroupProvider();
    }
    
    public HttpResponse get(long id) throws ProviderException {
        try{
            return (HttpResponse)ok(new UserGroupDto.Builder().userGroup(provider.getGroup(id)).build());
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse get() throws ProviderException {
        try{
            List<UserGroupDto> dtos = provider.getAllAvailableGroups()
                    .map(g -> new UserGroupDto.Builder().userGroup(g).build())
                    .collect(Collectors.toList());     
            return (HttpResponse)ok(dtos);
        } catch(ProviderException e) {
            return handle(e);
        }
    }

    public HttpResponse getAllUsers(long id) throws ProviderException {
        try{
            List<Long> dtos = provider.getAllGroupUsers(id)
                    .collect(Collectors.toList());     
            return (HttpResponse)ok(dtos);
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse updateUsers(long id, Set<Long> uids) throws ProviderException {
        try{
            provider.updateGroupUsers(id, uids);
            return ok("");
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse post(AddUserGroupDto dto) throws ProviderException {
        try{
            List<ErrorDto> validationErrors = AddUserGroupDto.validate(dto);
            if(!validationErrors.isEmpty())
                return badRequest(validationErrors);

            UserGroup newGroup = provider.createUserGroup(dto);
            return created("usergroups/"+newGroup.getId(), new UserGroupDto.Builder().userGroup(newGroup).build());
        } catch(ProviderException e) {
            if (e.getType() == ProviderException.Type.GROUP_ALREADY_EXISTS)
                return conflict(MessageKeys.ERR_USER_GROUP_ALREADY_EXISTS);
            return handle(e);
        }
    }
    
    public HttpResponse put(long id, AddUserGroupDto dto) throws ProviderException {
        try {
            List<ErrorDto> validationErrors = AddUserGroupDto.validate(dto);
            if (!validationErrors.isEmpty())
                return badRequest(validationErrors);
            
            provider.updateUserGroup(id, dto);
            return ok("");
        } catch (ProviderException e) {
            if (e.getType() == ProviderException.Type.GROUP_ALREADY_EXISTS)
                return conflict(MessageKeys.ERR_USER_GROUP_ALREADY_EXISTS);
            return handle(e);
        }
    }
    
    public HttpResponse delete(long id) throws ProviderException {
        try {
            provider.deleteUserGroup(id);
            return ok("");
        } catch (ProviderException e) {
            if (e.getType() == ProviderException.Type.DELETING_DEFAULT)
                return badRequest(Collections.singletonList(new ErrorDto(MessageKeys.ERR_USER_GROUP_DELETING_DEFAULT)));
            return handle(e);
        }
    }
}
