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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import pl.datamatica.traccar.api.dtos.JsonIgnore;
import pl.datamatica.traccar.api.dtos.in.EditDeviceDto;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Position;

public class DeviceDto extends EditDeviceDto implements ICachedDto {
    private final long id;
    private final String status;
    private final String uniqueId;
    private final PositionDto lastPosition;
    private final long accountId;
    private final Date validTo;
    private final Integer historyLength;
    private final boolean blocked;
    private final Integer batteryLevel;
    private final Date batteryTime;
    private final int batteryTimeout;
    
    @JsonIgnore
    private final Date modificationTime;

    public static class Builder {

        private long id;
        private String deviceName;
        private long deviceModelId;
        private Long iconId;
        private String color;
        private String phoneNumber;
        private String plateNumber;
        private String description;
        private String status;
        private String uniqueId;
        private PositionDto lastPosition;
        private long accountId;
        private Date validTo;
        private Integer historyLength;
        private Date modificationTime;
        private boolean blocked;
        private Double speedLimit;
        private Integer batteryLevel;
        private Date batteryTime;
        private int batteryTimeout;

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

        public Builder iconId(final Long value) {
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

        public Builder accountId(final long value) {
            this.accountId = value;
            return this;
        }
        
        public Builder validTo(final Date value) {
            this.validTo = value;
            return this;
        }
        
        public Builder historyLength(final int value) {
            this.historyLength = value;
            return this;
        }

        public Builder modificationTime(final Date value) {
            this.modificationTime = value;
            return this;
        }
        
        public Builder blocked(final boolean blocked) {
            this.blocked = blocked;
            return this;
        }
        
        public Builder speedLimit(final Double speedLimit) {
            this.speedLimit = speedLimit;
            return this;
        }
        
        public Builder batteryLevel(final Integer batteryLevel) {
            this.batteryLevel = batteryLevel;
            return this;
        }
        
        public Builder batteryTime(final Date time) {
            this.batteryTime = time;
            return this;
        }
        
        public Builder batteryTimeout(final int timeout) {
            this.batteryTimeout = timeout;
            return this;
        }
        
        private static final Double KilometersToNauticMilesMultiplier = 1.852;

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
            if(!device.isBlocked() && latestPosition != null)
                this.lastPosition = new PositionDto.Builder().position(latestPosition).build();
            this.accountId = device.getOwner().getId();
            if(device.getValidTo() != null) {
                LocalDate date = device.getValidTo().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDate().plusDays(1);
                this.validTo = Date.from(date.atStartOfDay()
                        .atZone(ZoneId.systemDefault()).toInstant());
            }
            this.historyLength = device.getHistoryLength();
            this.modificationTime = device.getLastUpdate();
            this.blocked = device.isBlocked();
            if(device.getSpeedLimit() != null)
                this.speedLimit = device.getSpeedLimit() * KilometersToNauticMilesMultiplier;
            this.batteryLevel = device.getBatteryLevel();
            this.batteryTime = device.getBatteryTime();
            this.batteryTimeout = device.getBatteryTimeout();
            return this;
        }

        public DeviceDto build() {
            return new DeviceDto(id, 
                    deviceName, 
                    deviceModelId, 
                    iconId, 
                    color, 
                    phoneNumber, 
                    plateNumber,
                    description,
                    status, 
                    uniqueId, 
                    lastPosition,
                    accountId,
                    validTo, 
                    historyLength,
                    modificationTime,
                    blocked,
                    speedLimit,
                    batteryLevel,
                    batteryTime,
                    batteryTimeout);
        }
    }

    private DeviceDto(final long id, 
            final String deviceName, 
            final long deviceModelId, 
            final Long iconId, 
            final String color, 
            final String phoneNumber, 
            final String plateNumber, 
            final String description, 
            final String status, 
            final String uniqueId,
            final PositionDto lastPosition, 
            final long accountId, 
            final Date validTo,
            final Integer historyLength,
            final Date modificationTime,
            final boolean blocked,
            final Double speedLimit,
            final Integer batteryLevel,
            final Date batteryTime,
            final int batteryTimeout) {
        super(deviceName, deviceModelId, iconId, color, phoneNumber, plateNumber, description, speedLimit);
        this.id = id;
        this.status = status;
        this.uniqueId = uniqueId;
        this.lastPosition = lastPosition;
        this.accountId = accountId;
        this.validTo = validTo;
        this.historyLength = historyLength;
        this.modificationTime = modificationTime;
        this.blocked = blocked;
        this.batteryLevel = batteryLevel;
        this.batteryTime = batteryTime;
        this.batteryTimeout = batteryTimeout;
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

    public Date getValidTo() {
        return validTo;
    }

    public int getHistoryLength() {
        return historyLength;
    }
    
    @Override
    public Date getModificationTime() {
        return modificationTime;
    }
}
