/*
 *  Copyright (C) 2017  Datamatica (dev@datamatica.pl)
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *  
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *  
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pl.datamatica.traccar.api.providers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.metadata.model.DeviceModel;
import pl.datamatica.traccar.api.services.CommandService;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author piotrkrzeszewski
 */
public class CommandsProvider extends ProviderBase {
    
    public static final String[] VALID_PARAM_KEYS= {
            "cmd_param_battery", "cmd_param_gprs", "cmd_param_gsm", "cmd_param_power", "cmd_param_gps", 
            "cmd_param_acc", "cmd_param_oil", "cmd_param_position_t", "cmd_param_number_a", "cmd_param_number_b", 
            "cmd_param_number_c", "cmd_param_time_zone", "cmd_param_overspeed_threshold", 
            "cmd_param_movement_alarm", "cmd_param_vibration_alarm", "cmd_param_defense", 
            "cmd_param_defense_time", "cmd_param_sends", "cmd_param_sensorset", "cmd_param_position_d", 
            "cmd_param_imei", "cmd_param_data_link", "cmd_param_roaming", "cmd_param_init_time",
            "cmd_param_rtc_time"
        };
    
    private final User requestUser;
    private final DeviceProvider deviceProvider;
    private final DeviceModelProvider deviceModelProvider;
    private final ActiveDeviceProvider activeDeviceProvider;
    private final BackendCommandProvider backendCommandProvider;
    
    public CommandsProvider(EntityManager em, User requestUser, DeviceProvider dp, DeviceModelProvider dmp) {
        super(em);
        this.requestUser = requestUser;
        this.deviceProvider = dp;
        this.deviceModelProvider = dmp;

        activeDeviceProvider = new ActiveDeviceProvider();
        backendCommandProvider = new BackendCommandProvider();
    }
    
    public Map<String, Object> superstatus(long deviceId) throws ProviderException {
        if (!requestUser.hasPermission(UserPermission.COMMAND_TCP))
            throw new ProviderException(ProviderException.Type.ACCESS_DENIED);
        
        Device device = deviceProvider.getDevice(deviceId);
        DeviceModel model = deviceModelProvider.getDeviceModel(device.getDeviceModelId());

        Object activeDevice = activeDeviceProvider.getActiveDevice(deviceId);
        if (activeDevice == null) 
           throw new ProviderException(ProviderException.Type.NOT_FOUND);
        
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

            if (tmp != null && tmp.get("success") != null && (boolean) tmp.get("success")) {
                result.put(type, tmp.get("response").toString());
            } 
        }
        return result;
        
    }
    
}
