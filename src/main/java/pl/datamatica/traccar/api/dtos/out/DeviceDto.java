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
package pl.datamatica.traccar.api.dtos.out;

import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Position;

public class DeviceDto {
    private final long id;
    private String deviceName;
    private long deviceModelId;
    private String iconId;
    private String color;
    private String phoneNumber;
    private String plateNumber;
    private String description;
    private String status;
    private String uniqueId;
    private PositionDto lastPosition;
    private boolean isDeleted;
    private long accountId;
    
    public DeviceDto(Device device) {
        this.id = device.getId();
        this.deviceName = device.getName();
        this.deviceModelId = device.getDeviceModelId();
        this.iconId = device.getIconType().toString();
        this.color = device.getColor();
        this.phoneNumber = device.getPhoneNumber();
        this.plateNumber = device.getPlateNumber();
        this.description = device.getDescription();
        this.status = device.getStatus();
        this.uniqueId = device.getUniqueId();
        Position latestPosition = device.getLatestPosition();
        if(latestPosition != null)
            this.lastPosition = new PositionDto(latestPosition);
        this.isDeleted = device.isDeleted();
        this.accountId = device.getOwner().getId();
    }
    
    public long getId() {
        return id;
    }

    public String getIconId() {
        return iconId;
    }

    public String getName() {
        return deviceName;
    }

    public PositionDto getLatestPosition() {
        return lastPosition;
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
        return lastPosition;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getDeviceModelId() {
        return deviceModelId;
    }

    public String getColor() {
        return color;
    }

    public PositionDto getLastPosition() {
        return lastPosition;
    }

    public boolean isDeleted() {
        return isDeleted;
    }
}
