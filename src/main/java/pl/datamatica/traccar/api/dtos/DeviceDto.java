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
    private String deviceName;
    private String phoneNumber;
    private String plateNumber;
    private String description;
    private String status;
    private String uniqueId;
    private PositionDto lastEvent;
    private long accountId;
    private Long groupId;
    
    public DeviceDto(Device device) {
        this.id = device.getId();
        this.iconKey = device.getIconType().toString();
        this.deviceName = device.getName();
        this.phoneNumber = device.getPhoneNumber();
        this.plateNumber = device.getPlateNumber();
        this.description = device.getDescription();
        this.status = device.getStatus();
        this.uniqueId = device.getUniqueId();
        this.accountId = device.getOwner().getId();
        if(device.getGroup() != null)
            this.groupId = device.getGroup().getId();
        Position latestPosition = device.getLatestPosition();
        if(latestPosition != null) {
            this.lastEvent = new PositionDto(latestPosition);
        }
    }
    
    public long getId() {
        return id;
    }

    public String getIconKey() {
        return iconKey;
    }

    public String getName() {
        return deviceName;
    }

    public PositionDto getLatestPosition() {
        return lastEvent;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public PositionDto getLastEvent() {
        return lastEvent;
    }

    public long getAccountId() {
        return accountId;
    }

    public Long getGroupId() {
        return groupId;
    }
    
    
}
