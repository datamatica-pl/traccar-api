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
import java.util.function.Function;
import java.util.stream.Collectors;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.Context;
import pl.datamatica.traccar.api.dtos.in.AddDeviceDto;
import pl.datamatica.traccar.api.dtos.out.DeviceDto;
import pl.datamatica.traccar.api.providers.DeviceProvider;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;
import spark.Spark;

public class DevicesController extends ControllerBase {
    
    private DeviceProvider dp;
    
    public DevicesController(RequestContext requestContext) {
        this(requestContext, ctx -> new DeviceProvider(ctx.getEntityManager()));
    }
    
    public DevicesController(RequestContext rc, Function<RequestContext, DeviceProvider> dpf) {
        super(rc);
        this.dp = dpf.apply(rc);
    }
    
    public HttpResponse get() throws Exception {
        User user = requestUser();
        List<DeviceDto> devices = dp.getAllAvailableDevices(user)
                .map(d -> new DeviceDto(d))
                .collect(Collectors.toList());
        
        return okCached(devices);
    }
    
    public HttpResponse get(long id) throws Exception {
        Device device = dp.getDevice(id);

        if(device == null)
            return notFound();
        if(dp.isVisibleToUser(device, requestUser())) 
            return okCached(new DeviceDto(device));
        else
            return forbidden();
    }
    
    public HttpResponse post(AddDeviceDto deviceDto) throws Exception {
        if(deviceDto == null || deviceDto.getImei() == null)
            return badRequest();
        //todo - createDevice error handling
        Device device = dp.createDevice(deviceDto.getImei(), requestUser());            
        return created("devices/"+device.getId(), device);
    }
    
    public static void registerMethods() {
        Gson gson = Context.getInstance().getGson();
        
        Spark.get(rootUrl(), (req, res) -> { 
            RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
            DevicesController dc = new DevicesController(context);
            return render(dc.get(), res);
        }, gson::toJson);
        
        Spark.post(rootUrl(), (req, res) -> {
            RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
            DevicesController dc = new DevicesController(context);
            AddDeviceDto deviceDto = gson.fromJson(req.body(), AddDeviceDto.class);
            return render(dc.post(deviceDto), res);
        });
        
        Spark.get(rootUrl()+"/:id", (req, res) -> {            
            RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
            DevicesController dc = new DevicesController(context);
            return render(dc.get(Long.parseLong(req.params(":id"))), res);
        }, gson::toJson);
    }
    
    public static String rootUrl() {
        return ControllerBase.rootUrl() + "/devices";
    }
}
