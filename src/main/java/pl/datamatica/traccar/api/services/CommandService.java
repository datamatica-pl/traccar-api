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
package pl.datamatica.traccar.api.services;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import pl.datamatica.traccar.api.CommandHandler;

public class CommandService {
    
    public String sendCommand(Long deviceId, String commandType, Object activeDevice, Map<String, Object> commandParams) throws Exception {
        final Map<String, Object> result = new HashMap<>();
        
        Class<?> backendCommandClass;
        Object backendCommand;
        try {
            backendCommandClass = Class.forName("org.traccar.model.Command");
            backendCommand = backendCommandClass.newInstance();
            backendCommand.getClass().getMethod("setType", String.class)
                    .invoke(backendCommand, commandType);
            backendCommand.getClass().getMethod("setDeviceId", long.class)
                    .invoke(backendCommand, deviceId); // TODO: activeDevice.deviceId
        } catch (Exception e) {
            return e.getMessage();
        }
        
        ArrayList<String> paramErrors = new ArrayList();
        commandParams.forEach((key,value) -> {
            try {
                backendCommand.getClass().getMethod("set", String.class, String.class)
                    .invoke(backendCommand, key, value);
            } catch (Exception e) {
                paramErrors.add(e.getMessage());
            }
        });

        if (paramErrors.isEmpty()) {
            try {
                final Object awaiter = new Object();
                Method sendCommand = activeDevice.getClass().getDeclaredMethod("sendCommand", backendCommandClass, Object.class);
                sendCommand.invoke(activeDevice, backendCommand, new CommandHandler(result, awaiter));
                synchronized(awaiter) {
                    awaiter.wait();
                }
            } catch (Exception e) {
                return "Error! Command cannot be sent.";
            }

            if (result.get("success") == null) {
                return "Error! Command cannot be sent.";
            } else if((boolean)result.get("success")) {
                return result.get("response").toString();
            } else {
                return "timeout";
            }
        } else {
            return "Incorrect command parameter, check if parameters are string.";
        }
    }
    
}
