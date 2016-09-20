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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static java.util.stream.Collectors.toList;
import pl.datamatica.traccar.api.Application;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.metadata.model.DeviceModel;
import pl.datamatica.traccar.api.providers.DeviceModelProvider;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.api.responses.OkCachedResponse;
import spark.Request;
import spark.Spark;

/**
 *
 * @author Jan Usarek
 */
public class DeviceModelsController extends ControllerBase {
    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {

            Spark.get(baseUrl(), (req, res) -> {
                DeviceModelsController dmc = createController(req);
                return render(dmc.get(), res);
            }, gson::toJson);

        }

        private DeviceModelsController createController(Request req) {
            RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
            DeviceModelsController rc = new DeviceModelsController(context);
            return rc;
        }
        
        public String baseUrl() {
            return resourcesUrl() + "/devicemodels";
        }
    }
    
    DeviceModelProvider provider;
    
    public DeviceModelsController(RequestContext rc) {
        super(rc);
        provider = rc.getDeviceModelProvider();
    }
    
    public HttpResponse get() throws Exception {
        List<DeviceModel> deviceModels = provider.getDeviceModelsMetadata();
        Date lastModified = new Date();
        if (deviceModels.size() > 0) {
            lastModified = (Date)deviceModels.get(0).getUpdateTime();
        }
        if (this.requestContext.getModificationDate() != null) {
            Timestamp ifModifiedSinceFromUser = new Timestamp(this.requestContext.getModificationDate().getTime());
            deviceModels = deviceModels.stream()
                            .filter( item -> item.getUpdateTime().compareTo(ifModifiedSinceFromUser) > 0 )
                            .collect(toList());
        }

        return new OkCachedResponse(deviceModels, lastModified);
    }
    
}
