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
import pl.datamatica.traccar.api.dtos.out.UserGroupDto;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.providers.UserGroupProvider;
import pl.datamatica.traccar.api.responses.HttpResponse;
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
                UserGroupsController dgc = createController(req);
                return render(dgc.get(), res);
            }, gson::toJson);
            
            Spark.get(baseUrl()+"/:id", (req, res) -> {
                UserGroupsController dgc = createController(req);
                return render(dgc.get(Long.parseLong(req.params(":id"))), res);
            }, gson::toJson);
            
//            Spark.post(baseUrl(), (req, res) -> {
//                DeviceGroupController dgc = createController(req);
//                AddDeviceGroupDto deviceDto = gson.fromJson(req.body(), AddDeviceGroupDto.class);
//                return render(dgc.post(deviceDto), res);
//            }, gson::toJson);
//            
//            Spark.put(baseUrl()+"/:id", (req, res) -> {
//                DeviceGroupController dgc = createController(req);
//                AddDeviceGroupDto dto = gson.fromJson(req.body(), AddDeviceGroupDto.class);
//                return render(dgc.put(Long.parseLong(req.params(":id")), dto), res);
//            }, gson::toJson);
//            
//            Spark.delete(baseUrl()+"/:id", (req, res) -> {
//                DeviceGroupController dgc = createController(req);
//                return render(dgc.delete(Long.parseLong(req.params(":id"))), res);
//            }, gson::toJson);
        }

        private UserGroupsController createController(Request req) {
            RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
            UserGroupsController dgc = new UserGroupsController(context);
            return dgc;
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
    
    public HttpResponse get() throws Exception {
        try{
            List<UserGroupDto> dtos = provider.getAllAvailableGroups()
                    .map(g -> new UserGroupDto.Builder().userGroup(g).build())
                    .collect(Collectors.toList());     
            return (HttpResponse)ok(dtos);
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse get(long id) throws Exception {
        try{
            return (HttpResponse)ok(new UserGroupDto.Builder().userGroup(provider.getGroup(id)).build());
        } catch(ProviderException e) {
            return handle(e);
        }
    }
}
