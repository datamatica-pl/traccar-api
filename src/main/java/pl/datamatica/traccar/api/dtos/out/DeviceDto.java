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

import java.util.Date;
import pl.datamatica.traccar.api.dtos.JsonIgnore;
import pl.datamatica.traccar.api.dtos.in.EditDeviceDto;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Position;

public class DeviceDto extends EditDeviceDto implements ICachedDto {
    private final long id;
    private String status;
    private String uniqueId;
    private PositionDto lastPosition;
    private Date oldestPositionTime;
    private boolean isDeleted;
    private long accountId;
    
    @JsonIgnore
    private Date modificationTime;

    public static class Builder {

        private long id;
        private String deviceName;
        private long deviceModelId;
        private long iconId;
        private String color;
        private String phoneNumber;
        private String plateNumber;
        private String description;
        private String status;
        private String uniqueId;
        private PositionDto lastPosition;
        private Date oldestPositionTime;
        private boolean isDeleted;
        private long accountId;
        private Date modificationTime;

        public Builder id(final long value) {
            this.id = value;
            return this;
        }

        public Builder deviceName(final String value) {
            this.deviceName = value;
            return this;
        }

        public Builder deviceModelId(final long value) {
            this.deviceModelId = value;
            return this;
        }

        public Builder iconId(final int value) {
            this.iconId = value;
            return this;
        }

        public Builder color(final String value) {
            this.color = value;
            return this;
        }

        public Builder phoneNumber(final String value) {
            this.phoneNumber = value;
            return this;
        }

        public Builder plateNumber(final String value) {
            this.plateNumber = value;
            return this;
        }

        public Builder description(final String value) {
            this.description = value;
            return this;
        }

        public Builder status(final String value) {
            this.status = value;
            return this;
        }

        public Builder uniqueId(final String value) {
            this.uniqueId = value;
            return this;
        }

        public Builder lastPosition(final PositionDto value) {
            this.lastPosition = value;
            return this;
        }

        public Builder oldestPositionTime(final Date value) {
            this.oldestPositionTime = value;
            return this;
        }

        public Builder isDeleted(final boolean value) {
            this.isDeleted = value;
            return this;
        }

        public Builder accountId(final long value) {
            this.accountId = value;
            return this;
        }

        public Builder modificationTime(final Date value) {
            this.modificationTime = value;
            return this;
        }
        
        public Builder device(final Device device) {
            this.id = device.getId();
            this.deviceName = device.getName();
            this.deviceModelId = device.getDeviceModelId();
            this.iconId = device.getIconId();
            this.color = device.getColor();
            this.phoneNumber = device.getPhoneNumber();
            this.plateNumber = device.getPlateNumber();
            this.description = device.getDescription();
            this.status = device.getStatus();
            this.uniqueId = device.getUniqueId();
            Position latestPosition = device.getLatestPosition();
            if(latestPosition != null)
                this.lastPosition = new PositionDto.Builder().position(latestPosition).build();
            this.isDeleted = device.isDeleted();
            this.accountId = device.getOwner().getId();
            if(device.getPositions() != null) 
                this.oldestPositionTime = device.getPositions().stream()
                        .map(p -> p.getTime())
                        .min((d1, d2) -> d1.compareTo(d2))
                        .orElse(null);
            this.modificationTime = device.getLastUpdate();
            return this;
        }

        public DeviceDto build() {
            return new DeviceDto(id, deviceName, deviceModelId, iconId, color, phoneNumber, plateNumber, description, status, uniqueId, lastPosition, oldestPositionTime, isDeleted, accountId, modificationTime);
        }
    }

    private DeviceDto(final long id, 
            final String deviceName, 
            final long deviceModelId, 
            final long iconId, 
            final String color, 
            final String phoneNumber, 
            final String plateNumber, 
            final String description, 
            final String status, 
            final String uniqueId,
            final PositionDto lastPosition, 
            final Date oldestPositionTime,
            final boolean isDeleted, 
            final long accountId, 
            final Date modificationTime) {
        super(deviceName, deviceModelId, iconId, color, phoneNumber, plateNumber, description);
        this.id = id;
        this.status = status;
        this.uniqueId = uniqueId;
        this.lastPosition = lastPosition;
        this.oldestPositionTime = oldestPositionTime;
        this.isDeleted = isDeleted;
        this.accountId = accountId;
        this.modificationTime = modificationTime;
    }
    
    public long getId() {
        return id;
    }

    public PositionDto getLatestPosition() {
        return lastPosition;
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

    public PositionDto getLastPosition() {
        return lastPosition;
    }

    public Date getOldestPositionTime() {
        return oldestPositionTime;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public Date getModificationTime() {
        return modificationTime;
    }
}
