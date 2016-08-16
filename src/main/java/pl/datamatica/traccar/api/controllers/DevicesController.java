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
import pl.datamatica.traccar.api.providers.DeviceProvider;
import pl.datamatica.traccar.api.transformers.DeviceTransformer;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;
import spark.Spark;

public class DevicesController extends ControllerBase<Device> {
    
    private DeviceProvider dp;
    
    public DevicesController(RequestContext cachingHandler) {
        this(Context.getInstance(), cachingHandler);
    }
    
    public DevicesController(Context context, RequestContext requestContext) {
        super(requestContext);
        dp = new DeviceProvider(context.getEntityManager());
    }
    
    public List<Device> get() {
        User user = requestContext.getUser();
        List<Device> devices = dp.getAllAvailableDevices(user).collect(Collectors.toList());
        
        return ok(devices);
    }
    
    public Device get(long id) {
        Device device = dp.getDevice(id);
        
        if(device == null) {
            return notFound();
        } else if(!DeviceProvider.isVisibleToUser(device, requestContext.getUser())) {
            return forbidden();
        }
        
        return ok(device);
    }
    
    public static void registerMethods() {
        Gson gson = Context.getInstance().getGson();
        DeviceTransformer responseTransformer = new DeviceTransformer(gson);
        
        Spark.get("devices", (req, res) -> { 
            RequestContext context = new RequestContext(req, res);
            DevicesController dc = new DevicesController(context);
            return dc.get();
        }, responseTransformer);
        
        Spark.get("devices/:id", (req, res) -> {            
            RequestContext context = new RequestContext(req, res);
            DevicesController dc = new DevicesController(context);
            return dc.get(Long.parseLong(req.params(":id")));
        }, responseTransformer);
    }
}
