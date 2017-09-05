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
import java.util.List;
import java.util.stream.Collectors;
import pl.datamatica.traccar.api.Application;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.dtos.in.AddDeviceGroupDto;
import pl.datamatica.traccar.api.dtos.out.DeviceGroupDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.providers.DeviceGroupProvider;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.model.Group;
import pl.datamatica.traccar.model.UserPermission;
import spark.Request;
import spark.Spark;

/**
 *
 * @author piotrkrzeszewski
 */
public class DeviceGroupController extends ControllerBase {
    
    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {

            Spark.get(baseUrl(), (req, res) -> {
                DeviceGroupController dgc = createController(req);
                return render(dgc.get(), res);
            }, gson::toJson);
            
            Spark.get(baseUrl()+"/:id", (req, res) -> {
                DeviceGroupController dgc = createController(req);
                return render(dgc.get(Long.parseLong(req.params(":id"))), res);
            }, gson::toJson);
            
            Spark.post(baseUrl(), (req, res) -> {
                DeviceGroupController dgc = createController(req);
                AddDeviceGroupDto deviceDto = gson.fromJson(req.body(), AddDeviceGroupDto.class);
                return render(dgc.post(deviceDto), res);
            }, gson::toJson);
            
            Spark.put(baseUrl()+"/:id", (req, res) -> {
                DeviceGroupController dgc = createController(req);
                AddDeviceGroupDto dto = gson.fromJson(req.body(), AddDeviceGroupDto.class);
                return render(dgc.put(Long.parseLong(req.params(":id")), dto), res);
            }, gson::toJson);
            
            Spark.delete(baseUrl()+"/:id", (req, res) -> {
                DeviceGroupController dgc = createController(req);
                return render(dgc.delete(Long.parseLong(req.params(":id"))), res);
            }, gson::toJson);
            
            Spark.get(baseUrl()+"/:id/share", (req, res) -> {
                DeviceGroupController dgc = createController(req);
                long id = Long.parseLong(req.params(":id"));
                return render(dgc.getGroupShare(id), res);
            });
            
            Spark.put(baseUrl()+"/:id/share", (req, res) -> {
                DeviceGroupController dgc = createController(req);
                long id = Long.parseLong(req.params(":id"));
                List<Long> ids = gson.fromJson(req.body(), 
                        new TypeToken<List<Long>>() {}.getType());
                return render(dgc.updateGroupShare(id, ids), res);
            });
        }

        private DeviceGroupController createController(Request req) {
            RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
            DeviceGroupController dgc = new DeviceGroupController(context);
            return dgc;
        }
        
        public String baseUrl() {
            return super.rootUrl() + "/devicegroups";
        }
    }
    
    DeviceGroupProvider provider;
    
    public DeviceGroupController(RequestContext rc) {
        super(rc);
        provider = rc.getDeviceGroupProvider();
    }
    
    public HttpResponse get() throws Exception {
        try{
            List<DeviceGroupDto> dtos = provider.getAllAvailableGroups()
                    .map(g -> new DeviceGroupDto.Builder().deviceGroup(g).build())
                    .collect(Collectors.toList());     
            return (HttpResponse)ok(dtos);
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse get(long id) throws Exception {
        try{
            return (HttpResponse)ok(new DeviceGroupDto.Builder().deviceGroup(provider.getSingleGroup(id)).build());
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse post(AddDeviceGroupDto dto) throws Exception {
        List<ErrorDto> validationErrors = AddDeviceGroupDto.validate(dto);
        if(!validationErrors.isEmpty())
            return badRequest(validationErrors);
        
        try {
            Group newGroup = provider.createGroup(dto);
            return created("devicegroups/"+newGroup.getId(), new DeviceGroupDto.Builder().deviceGroup(newGroup).build());
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse put(long id, AddDeviceGroupDto dto) throws ProviderException {
        List<ErrorDto> validationErrors = AddDeviceGroupDto.validate(dto);
        if(!validationErrors.isEmpty())
            return badRequest(validationErrors);
        
        try {
            provider.updateGroup(id, dto);
            return ok("");
        } catch (ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse delete(long id) throws ProviderException {
        try {
            provider.deleteGroup(id);
            return ok("");
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse getGroupShare(long id) throws ProviderException {
        try {
            List<Long> shares = provider.getGroupShare(id);
            return ok(shares);
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse updateGroupShare(long id, List<Long> uids) throws ProviderException {
        try {
            provider.updateGroupShare(id, uids);
            return ok("");
        } catch(ProviderException e) {
            return handle(e);
        }
    }
}
