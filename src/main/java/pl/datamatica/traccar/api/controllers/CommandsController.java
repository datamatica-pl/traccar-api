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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pl.datamatica.traccar.api.Application;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.out.CommandResponseDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.metadata.model.DeviceModel;
import pl.datamatica.traccar.api.providers.ActiveDeviceProvider;
import pl.datamatica.traccar.api.providers.BackendCommandProvider;
import pl.datamatica.traccar.api.providers.CommandsProvider;
import pl.datamatica.traccar.api.responses.HttpStatuses;
import pl.datamatica.traccar.api.services.CommandService;
import pl.datamatica.traccar.api.utils.JsonUtils;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;
import spark.Request;
import spark.Spark;

/**
 *
 * @author Jan Usarek
 */
public class CommandsController extends ControllerBase {
    public static class Binder extends ControllerBinder {
        
        public static final String[] VALID_PARAM_KEYS= {
            "cmd_param_battery", "cmd_param_gprs", "cmd_param_gsm", "cmd_param_power", "cmd_param_gps", 
            "cmd_param_acc", "cmd_param_oil", "cmd_param_position_t", "cmd_param_number_a", "cmd_param_number_b", 
            "cmd_param_number_c", "cmd_param_time_zone", "cmd_param_overspeed_threshold", 
            "cmd_param_movement_alarm", "cmd_param_vibration_alarm", "cmd_param_defense", 
            "cmd_param_defense_time", "cmd_param_sends", "cmd_param_sensorset", "cmd_param_position_d", 
            "cmd_param_imei", "cmd_param_data_link", "cmd_param_roaming", "cmd_param_init_time",
            "cmd_param_rtc_time"
        };

        @Override
        public void bind() {

            Spark.post(rootUrl() + "/devices/:deviceId/sendCommand/:commandType", (req, res) -> {
                final RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
                final User requestUser = context.getUser();
                final Long deviceId = Long.valueOf(req.params(":deviceId"));
                final String commandType = req.params(":commandType");
                final String params = req.body();
                Map<String, Object> commandParams = new HashMap<>();
                Device device;

                if (!requestUser.hasPermission(UserPermission.COMMAND_TCP)) {
                    res.status(HttpStatuses.FORBIDDEN);
                    return getResponseError(MessageKeys.ERR_ACCESS_DENIED);
                }
                
                res.status(HttpStatuses.BAD_REQUEST);
                res.type("application/json");
                
                if(("custom".equals(commandType) || "extendedCustom".equals(commandType))
                        && !requestUser.hasPermission(UserPermission.COMMAND_CUSTOM)) {
                    res.status(HttpStatuses.FORBIDDEN);
                    return getResponseError(MessageKeys.ERR_ACCESS_DENIED);
                }

                try {
                    device = context.getDeviceProvider().getDevice(deviceId);
                } catch (ProviderException e) {
                     device = null;
                }

                if (device == null && !requestUser.hasAccessTo(device)) {
                    res.status(HttpStatuses.NOT_FOUND);
                    return getResponseError(MessageKeys.ERR_DEVICE_NOT_FOUND_OR_NO_PRIVILEGES);
                }

                if (params != null) {
                    try {
                        commandParams = JsonUtils.getCommandParams(params);
                    } catch (Exception e) {
                        return getResponseError(MessageKeys.ERR_COMMAND_PARSE_PARAMS_FAILED);
                    }
                }
                commandParams.put("userId", context.getUser().getId());

                ActiveDeviceProvider adp = new ActiveDeviceProvider();
                Object activeDevice = adp.getActiveDevice(deviceId);
                if (activeDevice == null) {
                    res.status(HttpStatuses.NOT_FOUND);
                    return getResponseError(MessageKeys.ERR_ACTIVE_DEVICE_NOT_FOUND);
                }
                
                CommandService cs = new CommandService();
                Map<String, Object> result;
                
                if("custom".equals(commandType)) {
                    result = cs.sendCustomCommand(activeDevice, commandParams.get("command").toString());
                } else {
                    BackendCommandProvider bcp = new BackendCommandProvider();
                    Object backendCommand = null;
                    try {
                        backendCommand = bcp.getBackendCommand(deviceId, commandType);
                    } catch (Exception e) {
                        return getResponseError(MessageKeys.ERR_CREATE_COMMAND_OBJECT_FAILED);
                    }

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
                            return getResponseError(MessageKeys.ERR_SET_COMMAND_ATTRIBUTES_FAILED);
                        }
                    }

                    result = cs.sendCommand(activeDevice, backendCommand);
                }

                if (result == null || result.get("success") == null) {
                    return getResponseError(MessageKeys.ERR_SEND_COMMAND_FAILED);
                }

                if ((boolean) result.get("success")) {
                    CommandResponseDto commandResponse = new CommandResponseDto(result.get("response").toString());
                    res.status(HttpStatuses.OK);
                    return commandResponse;
                } else {
                    if (result.get("reason") == "timeout") {
                        res.status(HttpStatuses.TIMED_OUT);
                        return getResponseError(MessageKeys.ERR_COMMAND_RESPONSE_TIMEOUT);
                    } else {
                        return getResponseError(MessageKeys.ERR_SEND_COMMAND_FAILED);
                    }
                }
            }, gson::toJson);
            
            Spark.get(rootUrl()+"/devices/:deviceId/superstatus", (req, res) -> {
                
                CommandsController cc = createController(req);
                return render(cc.superstatus(Long.parseLong(req.params(":id"))), res);
            }, gson::toJson);
        }

        private List<ErrorDto> getResponseError(String messageKey) {
            return Collections.singletonList(new ErrorDto(messageKey));
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

    private CommandsProvider provider;
    private User requestUser;
    

    public CommandsController(RequestContext rc) {
        super(rc);
        requestUser = rc.getUser();
        
    }
    
    public HttpResponse superstatus(long deviceId) throws ProviderException {
          
                res.status(HttpStatuses.OK);
                Map<String, Object> result = new HashMap<>();
                for(String type : model.getSuperStatusCommands()) {
                    if(type == null || type.isEmpty())
                        continue;
                    type = type.trim();
                    BackendCommandProvider bcp = new BackendCommandProvider();
                    Object backendCommand = null;
                    try {
                        backendCommand = bcp.getBackendCommand(deviceId, type);
                    } catch (Exception e) {
                        result.put(type, MessageKeys.ERR_CREATE_COMMAND_OBJECT_FAILED);
                        continue;
                    }

                    CommandService cs = new CommandService();

                    Map<String, Object> tmp = cs.sendCommand(activeDevice, backendCommand);
                    
                    if (tmp == null || tmp.get("success") == null) {
                        result.put(type, "FAILED");
                    } else if ((boolean) tmp.get("success")) {
                        result.putAll(gson.fromJson(tmp.get("response").toString(), Map.class));
                    } else {
                        if (tmp.get("reason") == "timeout") {
                            result.put(type, "TIMEOUT");
                        } else {
                            result.put(type, "FAILED");
                        }
                    }
                }
                result.keySet().retainAll(Arrays.asList(VALID_PARAM_KEYS));
                return result;
    }
}
