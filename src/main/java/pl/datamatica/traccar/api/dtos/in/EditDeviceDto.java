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
import java.util.List;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;

public class EditDeviceDto {
    private final String deviceName;
    private final Long deviceModelId;
    private final Long iconId;
    private final String color;
    private final String phoneNumber;
    private final String plateNumber;
    private final String description;

    public static class Builder {

        private String deviceName;
        private long deviceModelId;
        private long iconId;
        private String color;
        private String phoneNumber;
        private String plateNumber;
        private String description;

        public Builder deviceName(final String value) {
            this.deviceName = value;
            return this;
        }

        public Builder deviceModelId(final long value) {
            this.deviceModelId = value;
            return this;
        }

        public Builder iconId(final long value) {
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

        public EditDeviceDto build() {
            return new EditDeviceDto(deviceName, deviceModelId, iconId, color, phoneNumber, plateNumber, description);
        }
    }

    protected EditDeviceDto(final String deviceName, 
            final long deviceModelId, 
            final long iconId, 
            final String color, 
            final String phoneNumber, 
            final String plateNumber, 
            final String description) {
        this.deviceName = deviceName;
        this.deviceModelId = deviceModelId;
        this.iconId = iconId;
        this.color = color;
        this.phoneNumber = phoneNumber;
        this.plateNumber = plateNumber;
        this.description = description;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public long getDeviceModelId() {
        return deviceModelId;
    }

    public long getIconId() {
        return iconId;
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
    
    public static List<ErrorDto> validate(EditDeviceDto deviceDto) {
        if(deviceDto == null)
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_DEVICE_DATA_NOT_PROVIDED));
        
        List<ErrorDto> errors = new ArrayList<>();
        if(deviceDto.color == null || deviceDto.color.isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_DEVICE_COLOR_NOT_PROVIDED));
        else if(!Application.COLOR_PATTERN.matcher(deviceDto.color).matches())
            errors.add(new ErrorDto(MessageKeys.ERR_INVALID_DEVICE_COLOR_FORMAT));
        if(deviceDto.deviceModelId == null)
            errors.add(new ErrorDto(MessageKeys.ERR_DEVICE_MODEL_ID_NOT_PROVIDED));
        if(deviceDto.deviceName == null || deviceDto.deviceName.isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_DEVICE_NAME_NOT_PROVIDED));
        if(deviceDto.iconId == null)
            errors.add(new ErrorDto(MessageKeys.ERR_DEVICE_ICON_ID_NOT_PROVIDED));
        return errors;
    }
}
