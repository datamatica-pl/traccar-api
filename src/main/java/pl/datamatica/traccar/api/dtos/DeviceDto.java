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
package pl.datamatica.traccar.api.dtos;

import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Position;

public class DeviceDto {
    private final long id;
    private String iconKey;
    private String name;
    private PositionDto latestPosition;
    
    public DeviceDto(Device device) {
        this.id = device.getId();
        this.iconKey = device.getIconType().toString();
        this.name = device.getName();
        Position latestPosition = device.getLatestPosition();
        if(latestPosition != null) {
            this.latestPosition = new PositionDto(latestPosition);
        }
    }
    
    public long getId() {
        return id;
    }

    public String getIconKey() {
        return iconKey;
    }

    public String getName() {
        return name;
    }

    public PositionDto getLatestPosition() {
        return latestPosition;
    }
}
