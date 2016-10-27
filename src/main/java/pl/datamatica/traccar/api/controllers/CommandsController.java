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

import java.util.HashMap;
import java.util.Map;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.providers.CommandDependencyProvider;
import pl.datamatica.traccar.api.services.CommandService;
import pl.datamatica.traccar.api.utils.JsonUtils;
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
            
            Spark.post(rootUrl() + "/devices/:deviceId/sendCommand/:commandType", (req, res) -> {
                Long deviceId = Long.valueOf(req.params(":deviceId"));
                String commandType = req.params(":commandType");
                String params = req.queryParams("params");
                Map<String, Object> commandParams = new HashMap<>();
                
                CommandDependencyProvider cdp = new CommandDependencyProvider();
                
                if (params != null) {
                    commandParams = JsonUtils.getCommandParams(params);
                }
                
                Object activeDevice = cdp.getActiveDevice(deviceId);
                if (activeDevice == null) {
                    return "Error! Device is not registered on the server.";
                }
                
                CommandService cs = new CommandService();
                String result = cs.sendCommand(deviceId, commandType, activeDevice, commandParams);

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
