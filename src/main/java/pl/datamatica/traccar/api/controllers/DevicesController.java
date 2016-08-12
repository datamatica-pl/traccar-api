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
import java.util.Date;
import java.util.stream.Stream;
import pl.datamatica.traccar.api.Context;
import pl.datamatica.traccar.api.providers.DeviceProvider;
import pl.datamatica.traccar.api.transformers.DeviceTransformer;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;
import spark.Spark;

public class DevicesController {
    
    private DeviceProvider dp;
    private RequestContext requestContext;
    
    public DevicesController(RequestContext cachingHandler) {
        this(Context.getInstance(), cachingHandler);
    }
    
    public DevicesController(Context context, RequestContext requestContext) {
        dp = new DeviceProvider(context.getEntityManager());
        this.requestContext = requestContext;
    }
    
    public Device[] get() {
        User user = requestContext.getUser();
        Device[] devices = dp.getAllAvailableDevices(user).toArray(Device[]::new);
        Date modificationTime = new Date(Stream.of(devices)
                .mapToLong(d -> d.getLastUpdate().getTime())
                .max()
                .orElse(0));
        
        requestContext.setLastModified(modificationTime);
        if(!requestContext.isModified()) {
            Spark.halt(304);
            return null;
        }
        
        return devices;
    }
    
    public Device get(long id) {
        Device device = dp.getDevice(id);
        
        if(device == null) {
            Spark.halt(404);
            return null;
        } else if(!DeviceProvider.isVisibleToUser(device, requestContext.getUser())) {
            Spark.halt(403);
            return null;
        }
        
        requestContext.setLastModified(device.getLastUpdate());
        if(!requestContext.isModified()) {
            Spark.halt(304);
            return null;
        }
        return device;
    }
    
    private boolean canShowDeviceToUser(Device device, User user) {
        return user.getAdmin() || user.getAllAvailableDevices().contains(device);
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
