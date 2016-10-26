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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.toList;
import pl.datamatica.traccar.api.Application;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.providers.CommandDependancyProvider;
//import pl.datamatica.traccar.api.metadata.model.ReportType;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.api.responses.OkCachedResponse;
import pl.datamatica.traccar.api.services.CommandService;
import spark.Request;
import spark.Spark;

/**
 *
 * @author Jan Usarek
 */
public class CommandsController extends ControllerBase {
    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {
            
            // TODO: Use POST to send command
//            Spark.post("devices/:deviceId/sendCommand/:commandType", (req, res) -> {
//                String commandType = req.params(":commandType");
//                Long deviceId = Long.valueOf(req.params(":deviceId"));
//                return deviceId + ":" + commandType;
//            });
            
            // TODO: Chagne to POST, this method is only temporary
            // https://localhost/api/v1/resources/sendCommand
            Spark.get(baseUrl(), (req, res) -> {
                long deviceId = 4; // LK210 #2
                
                CommandDependancyProvider cdp = new CommandDependancyProvider();
                
                Object activeDevice = cdp.getActiveDevice(deviceId);
                if (activeDevice == null) {
                    return "Error! Device is not registered on the server.";
                }
                
                Map<String, Object> commandParams = new HashMap<>();
                
                CommandService cs = new CommandService();
                String result = cs.sendCommand(deviceId, "getParams", activeDevice, commandParams);

                return result;
            }, gson::toJson);

        }

        private CommandsController createController(Request req) {
            RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
            CommandsController cc = new CommandsController(context);
            return cc;
        }
        
        public String baseUrl() {
            return resourcesUrl() + "/sendCommand";
        }
    }

    public CommandsController(RequestContext rc) {
        super(rc);
    }
    
}
