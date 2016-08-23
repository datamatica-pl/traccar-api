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

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import pl.datamatica.traccar.api.Application;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.in.AddDeviceDto;
import pl.datamatica.traccar.api.dtos.out.DeviceDto;
import pl.datamatica.traccar.api.providers.DeviceProvider;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.model.Device;
import spark.Spark;

public class DevicesController extends ControllerBase {
    
    public static class Binder extends ControllerBinder { 
        
        @Override
        public void bind() {
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
            }, gson::toJson);

            Spark.get(rootUrl()+"/:id", (req, res) -> {            
                RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
                DevicesController dc = new DevicesController(context);
                return render(dc.get(Long.parseLong(req.params(":id"))), res);
            }, gson::toJson);
        }
        
        @Override
        public String rootUrl() {
            return super.rootUrl() + "/devices";
        }
    }

    
    private final DeviceProvider dp;
    
    public DevicesController(RequestContext requestContext) {
        super(requestContext);
        this.dp = requestContext.getDeviceProvider();
    }
    
    public HttpResponse get() throws Exception {
        List<DeviceDto> devices = dp.getAllAvailableDevices()
                .map(d -> new DeviceDto(d))
                .filter(d -> isModified(d.getModificationTime()))
                .collect(Collectors.toList());
        
        return okCached(devices);
    }
    
    public HttpResponse get(long id) throws Exception {
        Device device = dp.getDevice(id);

        if(device == null)
            return notFound();
        if(dp.isVisible(device)) 
            return okCached(new DeviceDto(device));
        else
            return forbidden();
    }
    
    public HttpResponse post(AddDeviceDto deviceDto) throws Exception {
        if(deviceDto == null || deviceDto.getImei() == null)
            return badRequest(MessageKeys.ERR_IMEI_NOT_PROVIDED);
        Device device = dp.createDevice(deviceDto.getImei());   
        if(device == null)
            return badRequest(MessageKeys.ERR_INVALID_IMEI);
        return created("devices/"+device.getId(), device);
    }
}
