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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.out.CommandResponseDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.providers.ActiveDeviceProvider;
import pl.datamatica.traccar.api.providers.BackendCommandProvider;
import pl.datamatica.traccar.api.responses.ErrorResponse;
import pl.datamatica.traccar.api.responses.HttpStatuses;
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
                String params = req.body();
                Map<String, Object> commandParams = new HashMap<>();
                res.type("application/json");
                
                if (params != null) {
                    try {
                        commandParams = JsonUtils.getCommandParams(params);
                    } catch (Exception e) {
                        res.status(HttpStatuses.BAD_REQUEST);
                        return getResponseError(HttpStatuses.BAD_REQUEST, MessageKeys.ERR_COMMAND_PARSE_PARAMS_FAILED);
                    }
                }
                
                ActiveDeviceProvider adp = new ActiveDeviceProvider();
                Object activeDevice = adp.getActiveDevice(deviceId);
                if (activeDevice == null) {
                    res.status(HttpStatuses.NOT_FOUND);
                    return getResponseError(HttpStatuses.NOT_FOUND, MessageKeys.ERR_ACTIVE_DEVICE_NOT_FOUND);
                }
                
                BackendCommandProvider bcp = new BackendCommandProvider();
                Object backendCommand = null;
                try {
                    backendCommand = bcp.getBackendCommand(deviceId, commandType);
                } catch (Exception e) {
                    res.status(HttpStatuses.BAD_REQUEST);
                    return getResponseError(HttpStatuses.BAD_REQUEST, MessageKeys.ERR_CREATE_COMMAND_OBJECT_FAILED);
                }

                CommandService cs = new CommandService();
                
                if (commandParams.size() > 0) {
                    // Change timezone parameter from hours to seconds
                    if (commandParams.get("timezone") != null) {
                        long timezoneHours = Long.valueOf(commandParams.get("timezone").toString());
                        long timezoneSeconds = timezoneHours * 3600;
                        commandParams.replace("timezone", timezoneSeconds);
                    }
                    
                    try {
                        backendCommand
                            .getClass()
                            .getMethod("setAttributes", Map.class)
                            .invoke(backendCommand, commandParams);
                    } catch (Exception e) {
                        res.status(HttpStatuses.BAD_REQUEST);
                        return getResponseError(HttpStatuses.BAD_REQUEST, MessageKeys.ERR_SET_COMMAND_ATTRIBUTES_FAILED);
                    }
                }
                
                Map<String, Object> result = cs.sendCommand(activeDevice, backendCommand);
                
                if (result == null || result.get("success") == null) {
                    res.status(HttpStatuses.BAD_REQUEST);
                    return getResponseError(HttpStatuses.BAD_REQUEST, MessageKeys.ERR_SEND_COMMAND_FAILED);
                }
                
                if ((boolean) result.get("success")) {
                    CommandResponseDto commandResponse = new CommandResponseDto(result.get("response").toString());
                    return commandResponse;
                } else {
                    if (result.get("reason") == "timeout") {
                        res.status(HttpStatuses.TIMED_OUT);
                        return getResponseError(HttpStatuses.TIMED_OUT, MessageKeys.ERR_COMMAND_RESPONSE_TIMEOUT);
                    } else {
                        res.status(HttpStatuses.BAD_REQUEST);
                        return getResponseError(HttpStatuses.BAD_REQUEST, MessageKeys.ERR_SEND_COMMAND_FAILED);
                    }
                }
            }, gson::toJson);

        }
        
        private ErrorResponse getResponseError(int status, String messageKey) {
            return new ErrorResponse(status, Collections.singletonList(new ErrorDto(messageKey)));
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
