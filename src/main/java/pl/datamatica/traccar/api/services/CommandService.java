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
import java.util.HashMap;
import java.util.Map;
import pl.datamatica.traccar.api.CommandHandler;

public class CommandService {

    public Map<String, Object> sendCommand(Object activeDevice, Object backendCommand) {
        final Map<String, Object> result = new HashMap<>();
        final Object awaiter = new Object();
        
        try {
            Method sendCommand = activeDevice.getClass().getDeclaredMethod("sendCommand",
                    backendCommand.getClass(), Object.class);
            sendCommand.invoke(activeDevice, backendCommand, new CommandHandler(result, awaiter));
            synchronized (awaiter) {
                awaiter.wait();
            }
        } catch (Exception e) {
            return null;
        }
        
        return result;
    }
    
    public Map<String, Object> sendCustomCommand(Object activeDevice, String msg) {
        final Map<String, Object> result = new HashMap<>();
        final Object awaiter = new Object();
        
        try {
            Method sendCommand = activeDevice.getClass().getDeclaredMethod("write", Object.class,
                                Object.class);
            sendCommand.invoke(activeDevice, msg, new CommandHandler(result, awaiter));
            synchronized(awaiter) {
                awaiter.wait();
            }
        } catch(Exception e) {
            return null;
        }
        return result;
    }

}
