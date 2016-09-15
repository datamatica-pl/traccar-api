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
import pl.datamatica.traccar.api.metadata.model.DeviceIcon;
import pl.datamatica.traccar.api.providers.DeviceIconProvider;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.api.responses.OkCachedResponse;
import spark.Request;
import spark.Spark;

/**
 *
 * @author Jan Usarek
 */
public class DeviceIconsController extends ControllerBase {
    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {

            Spark.get(rootUrl(), (req, res) -> {
                DeviceIconsController dic = createController(req);
                return render(dic.get(), res);
            }, gson::toJson);

        }

        private DeviceIconsController createController(Request req) {
            RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
            DeviceIconsController dic = new DeviceIconsController(context);
            return dic;
        }

        @Override
        public String rootUrl() {
            return super.rootUrl() + "/deviceicons";
        }
    }
    
    DeviceIconProvider provider;
    
    public DeviceIconsController(RequestContext rc) {
        super(rc);
        provider = rc.getDeviceIconProvider();
    }
    
    public HttpResponse get() throws Exception {
        List<DeviceIcon> deviceIcons = new ArrayList<>();
        Date lastModified = new Date();
        try {
            requestContext.beginMetadataTransaction();
            deviceIcons = provider.getDeviceIconsMetadata();
            lastModified = (Date)deviceIcons.get(0).getUpdateTime();
            if (this.requestContext.getModificationDate() != null) {
                Timestamp ifModifiedSinceFromUser = new Timestamp(this.requestContext.getModificationDate().getTime());
                deviceIcons = deviceIcons.stream()
                                .filter( item -> item.getUpdateTime().compareTo(ifModifiedSinceFromUser) > 0 )
                                .collect(toList());
            }
            requestContext.commitMetadataTransaction();
        } catch (RuntimeException e) {
            requestContext.rollbackMetadataTransation();
            throw e;
        }
        
        return new OkCachedResponse(deviceIcons, lastModified);
    }
}
