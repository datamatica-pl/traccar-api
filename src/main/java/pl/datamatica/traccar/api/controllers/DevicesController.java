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
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.api.Context;
import pl.datamatica.traccar.api.dtos.DeviceDto;
import pl.datamatica.traccar.api.transformers.DeviceTransformer;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;
import spark.Spark;

public class DevicesController {
    
    private EntityManager em;
    private User user;
    
    public DevicesController(User user) {
        this(Context.getInstance(), user);
    }
    
    public DevicesController(Context context, User user) {
        em = context.getEntityManager();
        this.user = user;
    }
    
    public List<Device> get() {
        if(user.getAdmin()) {
            TypedQuery<Device> tq = em.createQuery("Select x from Device x", Device.class);
            return tq.getResultList();
        }
        return new ArrayList<>(user.getAllAvailableDevices());
    }
    
    public Device get(long id) {
        Device device = em.find(Device.class, id);
        if(!user.getAllAvailableDevices().contains(device))
            return null;
        return device;
    }
    
    public static void registerMethods() {
        Gson gson = Context.getInstance().getGson();
        DeviceTransformer responseTransformer = new DeviceTransformer(gson);
        
        Spark.get("devices", (req, res) -> {
            DevicesController dc = new DevicesController(req.session().attribute("user"));
            return dc.get();
        }, responseTransformer);
        
        Spark.get("devices/:id", (req, res) -> {
            DevicesController dc = new DevicesController(req.session().attribute("user"));
            return dc.get(Long.parseLong(req.params(":id")));
        }, responseTransformer);
    }
}
