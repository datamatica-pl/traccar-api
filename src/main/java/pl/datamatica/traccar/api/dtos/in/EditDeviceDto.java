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
package pl.datamatica.traccar.api.dtos.in;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.dtos.out.MaintenanceDto;

public class EditDeviceDto {
    private final String deviceName;
    private final Long deviceModelId;
    private final Long iconId;
    private final Long customIconId;
    private final String color;
    private final String phoneNumber;
    private final String plateNumber;
    private final String description;
    private final Double speedLimit;
    private final Double fuelCapacity;
    
    //web
    private final Long groupId;
    private final String vehicleInfo;
    private final Boolean autoUpdateOdometer;
    private final Integer timeout;
    private final Integer minIdleTime;
    private final Double idleSpeedThreshold;
    private final Integer timeZoneOffset;
    private final String commandPassword;
    private final Integer historyLength;
    private final Date validTo;
    private final Boolean showOdometer;
    private final Boolean showProtocol;
    //
    private final Boolean showName;
    private final Double arrowRadius;
    private final String arrowMovingColor;
    private final String arrowStoppedColor;
    private final String arrowPausedColor;
    private final String arrowOfflineColor;
    private final List<MaintenanceDto> maintenances;
    private final List<MaintenanceDto> registrations;

    public static class Builder {

        private String deviceName;
        private Long deviceModelId;
        private Long iconId;
        private Long customIconId;
        private String color;
        private String phoneNumber;
        private String plateNumber;
        private String description;
        private Double speedLimit;
        private Double fuelCapacity;
        //web
        private Long groupId;
        private String vehicleInfo;
        private boolean autoUpdateOdometer;
        private int timeout;
        private Integer minIdleTime;
        private Double idleSpeedThreshold;
        private int timeZoneOffset;
        private String commandPassword;
        private int historyLength;
        private Date validTo;
        private boolean showOdometer;
        private boolean showProtocol;
        //
        private boolean showName;
        private Double arrowRadius;
        private String arrowMovingColor;
        private String arrowStoppedColor;
        private String arrowPausedColor;
        private String arrowOfflineColor;
        private List<MaintenanceDto> maintenances;
        private List<MaintenanceDto> registrations;

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
        
        public Builder customIconId(final Long value) {
            this.customIconId = value;
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
        
        public Builder speedLimit(final Double value) {
            this.speedLimit = value;
            return this;
        }
        
        public Builder fuelCapacity(final Double value) {
            this.fuelCapacity = value;
            return this;
        }

        public EditDeviceDto build() {
            return new EditDeviceDto(deviceName, 
                    deviceModelId, 
                    iconId, 
                    customIconId,
                    color, 
                    phoneNumber, 
                    plateNumber, 
                    description,
                    speedLimit,
                    fuelCapacity,
                    groupId, minIdleTime, idleSpeedThreshold, historyLength, validTo,
                    vehicleInfo, autoUpdateOdometer, timeout, timeZoneOffset,commandPassword,
                    showOdometer, showProtocol, showName, arrowRadius, 
                    arrowMovingColor, arrowStoppedColor, arrowPausedColor, arrowOfflineColor, 
                    maintenances, registrations);
        }
    }

    protected EditDeviceDto(final String deviceName, 
            final Long deviceModelId, 
            final Long iconId, 
            final Long customIconId,
            final String color, 
            final String phoneNumber, 
            final String plateNumber, 
            final String description,
            final Double speedLimit,
            final Double fuelCapacity,
            final Long groupId,
            final Integer minIdleTime,
            final Double idleSpeedThreshold,
            final int historyLength,
            final Date validTo,
            final String vehicleInfo,
            final boolean autoUpdateOdometer,
            final int timeout,
            final int timeZoneOffset,
            final String commandPassword,
            final boolean showOdometer,
            final boolean showProtocol,
            final boolean showName,
            final Double arrowRadius,
            final String arrowMovingColor,
            final String arrowStoppedColor,
            final String arrowPausedColor,
            final String arrowOfflineColor,
            final List<MaintenanceDto> maintenances,
            final List<MaintenanceDto> registrations) {
        this.deviceName = deviceName;
        this.deviceModelId = deviceModelId;
        this.iconId = iconId;
        this.customIconId = customIconId;
        this.color = color;
        this.phoneNumber = phoneNumber;
        this.plateNumber = plateNumber;
        this.description = description;
        this.speedLimit = speedLimit;
        this.fuelCapacity = fuelCapacity;
        this.groupId = groupId;
        this.vehicleInfo = vehicleInfo;
        this.autoUpdateOdometer = autoUpdateOdometer;
        this.timeout = timeout;
        this.minIdleTime = minIdleTime;
        this.idleSpeedThreshold = idleSpeedThreshold;
        this.timeZoneOffset = timeZoneOffset;
        this.commandPassword = commandPassword;
        this.historyLength = historyLength;
        this.validTo = validTo;
        this.showOdometer = showOdometer;
        this.showProtocol = showProtocol;
        this.showName = showName;
        this.arrowRadius = arrowRadius;
        this.arrowMovingColor = arrowMovingColor;
        this.arrowStoppedColor = arrowStoppedColor;
        this.arrowPausedColor = arrowPausedColor;
        this.arrowOfflineColor = arrowOfflineColor;
        this.maintenances = new ArrayList<>();
        if(maintenances != null)
            this.maintenances.addAll(maintenances);
        this.registrations = new ArrayList<>();
        if(registrations != null)
            this.registrations.addAll(registrations);
    }

    public String getDeviceName() {
        return deviceName;
    }

    public long getDeviceModelId() {
        return deviceModelId;
    }

    public Long getIconId() {
        return iconId;
    }
    
    public Long getCustomIconId() {
        return customIconId;
    }

    public String getColor() {
        return color;
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
    
    public Double getSpeedLimit() {
        return speedLimit;
    }
    
    public Double getFuelCapacity() {
        return fuelCapacity;
    }

    public Double getArrowRadius() {
        return arrowRadius;
    }

    public String getArrowMovingColor() {
        return arrowMovingColor;
    }

    public String getArrowStoppedColor() {
        return arrowStoppedColor;
    }

    public String getArrowPausedColor() {
        return arrowPausedColor;
    }

    public String getArrowOfflineColor() {
        return arrowOfflineColor;
    }

    public Long getGroupId() {
        return groupId;
    }

    public boolean isAutoUpdateOdometer() {
        return autoUpdateOdometer;
    }

    public String getCommandPassword() {
        return commandPassword;
    }

    public Integer getMinIdleTime() {
        return minIdleTime;
    }

    public Double getIdleSpeedThreshold() {
        return idleSpeedThreshold;
    }

    public Integer getHistoryLength() {
        return historyLength;
    }

    public Boolean isShowName() {
        return showName;
    }

    public String getVehicleInfo() {
        return vehicleInfo;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public Integer getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public Date getValidTo() {
        return validTo;
    }

    public Boolean isShowOdometer() {
        return showOdometer;
    }

    public Boolean isShowProtocol() {
        return showProtocol;
    }
    
    public List<MaintenanceDto> getMaintenances() {
        return maintenances;
    }
    
    public List<MaintenanceDto> getRegistrations() {
        return registrations;
    }
    
    public static List<ErrorDto> validate(EditDeviceDto deviceDto) {
        if(deviceDto == null)
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_DATA_NOT_PROVIDED));
        
        List<ErrorDto> errors = new ArrayList<>();
        if(deviceDto.color == null || deviceDto.color.isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_DEVICE_COLOR_NOT_PROVIDED));
        else if(!Application.COLOR_PATTERN.matcher(deviceDto.color).matches())
            errors.add(new ErrorDto(MessageKeys.ERR_INVALID_DEVICE_COLOR_FORMAT));
        if(deviceDto.deviceModelId == null)
            errors.add(new ErrorDto(MessageKeys.ERR_DEVICE_MODEL_ID_NOT_PROVIDED));
        if(deviceDto.deviceName == null || deviceDto.deviceName.isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_DEVICE_NAME_NOT_PROVIDED));
        if(deviceDto.iconId == null && deviceDto.customIconId == null)
            errors.add(new ErrorDto(MessageKeys.ERR_DEVICE_ICON_ID_NOT_PROVIDED));
        return errors;
    }
}
