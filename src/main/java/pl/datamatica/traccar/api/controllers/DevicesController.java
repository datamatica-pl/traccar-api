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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.api.Context;
import pl.datamatica.traccar.api.dtos.DeviceDto;
import pl.datamatica.traccar.api.transformers.DeviceTransformer;
import pl.datamatica.traccar.api.utils.DateUtil;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;
import spark.Request;
import spark.Spark;

public class DevicesController {
    
    private EntityManager em;
    private User user;
    private CachingHandler cachingHandler;
    
    public DevicesController(User user, CachingHandler cachingHandler) {
        this(Context.getInstance(), user, cachingHandler);
    }
    
    public DevicesController(Context context, User user, CachingHandler cachingHandler) {
        em = context.getEntityManager();
        this.user = user;
        this.cachingHandler = cachingHandler;
    }
    
    public List<Device> get() {
        long modificationTimestamp = 0;
        List<Device> devices;
        if(user.getAdmin()) {
            TypedQuery<Device> tq = em.createQuery("Select x from Device x", Device.class);
            devices = tq.getResultList();
        } else {
            devices = new ArrayList<>(user.getAllAvailableDevices());
        }
        
        for(Device device:devices)
            if(device.getLastUpdate() != null && device.getLastUpdate().getTime() > modificationTimestamp)
                modificationTimestamp = device.getLastUpdate().getTime();
        cachingHandler.setLastModified(new Date(modificationTimestamp));
        if(!cachingHandler.isModified()) {
            Spark.halt(304);
            return null;
        }
        
        return devices;
    }
    
    public Device get(long id) {
        Device device = em.find(Device.class, id);
        
        if(device == null) {
            Spark.halt(404);
            return null;
        } else if(!canShowDeviceToUser(device, user)) {
            Spark.halt(403);
            return null;
        }
        
        cachingHandler.setLastModified(device.getLastUpdate());
        if(!cachingHandler.isModified()) {
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
            CachingHandler cachingHandler = new CachingHandler(req);
            DevicesController dc = new DevicesController(req.session().attribute("user"),
                    cachingHandler);
            List<Device> result = dc.get();
            cachingHandler.addLastModified(res);
            return result;
        }, responseTransformer);
        
        Spark.get("devices/:id", (req, res) -> {            
            CachingHandler cachingHandler = new CachingHandler(req);
            DevicesController dc = new DevicesController(req.session().attribute("user"),
                    cachingHandler);
            Device result = dc.get(Long.parseLong(req.params(":id")));
            cachingHandler.addLastModified(res);
            return result;
        }, responseTransformer);
    }
}
