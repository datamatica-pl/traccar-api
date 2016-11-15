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
package pl.datamatica.traccar.api.providers;

/**
 *
 * @author Jan Usarek
 */
public class BackendCommandProvider implements IBackendCommandProvider {

    @Override
    public Object getBackendCommand(long deviceId, String commandType) throws Exception {
        Class<?> backendCommandClass;
        Object backendCommand;
        
        backendCommandClass = Class.forName("org.traccar.model.Command");
        backendCommand = backendCommandClass.newInstance();
        backendCommand.getClass().getMethod("setType", String.class)
                .invoke(backendCommand, commandType);
        backendCommand.getClass().getMethod("setDeviceId", long.class)
                .invoke(backendCommand, deviceId);
        
        return backendCommand;
    }
    
}