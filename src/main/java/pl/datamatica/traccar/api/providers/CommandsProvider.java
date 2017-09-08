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

import javax.persistence.EntityManager;
import pl.datamatica.traccar.api.metadata.model.DeviceModel;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author piotrkrzeszewski
 */
public class CommandsProvider extends ProviderBase {
    
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
    
    public void superstatus(long deviceId) throws ProviderException {
        if (!requestUser.hasPermission(UserPermission.COMMAND_TCP))
            throw new ProviderException(ProviderException.Type.ACCESS_DENIED);
        
        Device device = deviceProvider.getDevice(deviceId);
        DeviceModel model = deviceModelProvider.getDeviceModel(device.getDeviceModelId());

        Object activeDevice = activeDeviceProvider.getActiveDevice(deviceId);
        if (activeDevice == null) 
           throw new ProviderException(ProviderException.Type.NOT_FOUND);
        
    }
    
}
