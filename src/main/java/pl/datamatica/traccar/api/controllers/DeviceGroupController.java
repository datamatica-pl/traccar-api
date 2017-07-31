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
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.dtos.out.DeviceGroupDto;
import pl.datamatica.traccar.api.providers.DeviceGroupProvider;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.model.Group;
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
    RequestContext requestContext;
    
    public DeviceGroupController(RequestContext rc) {
        super(rc);
        provider = rc.getDeviceGroupProvider();
        requestContext = rc;
    }
    
    public HttpResponse get() throws Exception {
        List<DeviceGroupDto> dtos = provider.getAllAvailableGroups()
                .map(g -> new DeviceGroupDto.Builder().deviceGroup(g).build())
                .filter(g -> true)
                .collect(Collectors.toList());
                
        return (HttpResponse)ok(dtos);
    }
    
    public HttpResponse get(long id) throws Exception {
        Group gr = provider.getGroup(id);
        DeviceGroupDto dto = new DeviceGroupDto.Builder().deviceGroup(gr).build();
        
        return (HttpResponse)ok(dto);
    }
}
