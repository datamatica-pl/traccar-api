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
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;

/**
 *
 * @author piotrkrzeszewski
 */
public class AddDeviceGroupDto {
    private final String description;
    private final String name;
    private final Long parentId;
    
    protected AddDeviceGroupDto(String description, String name, Long parent) {
        this.description = description;
        this.name = name;
        this.parentId = parent;
    }
    
    public static List<ErrorDto> validate(AddDeviceGroupDto dto) {
        if(dto == null)
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_APPSETTINGS_DATA_NOT_PROVIDED));
        
        List<ErrorDto> errors = new ArrayList<>();
        if (dto.name == null || dto.name.isEmpty()) {
            errors.add(new ErrorDto(MessageKeys.ERR_DEVICE_GROUP_NAME_NOT_PROVIDED));
        }
        
        return errors;
    }
    
    public static class Builder {
        private String description;
        private String name;
        private long parentId;
    
        public Builder() {
        }
        
        public Builder description(String value) {
            this.description = value;
            return this;
        }
        
        public Builder name(String value) {
            this.name = value;
            return this;
        }
        
        public Builder parent(long value) {
            this.parentId = value;
            return this;
        }
        
        public AddDeviceGroupDto build() {
            return new AddDeviceGroupDto(description, name, parentId);
        }
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Long getParentId() {
        return parentId;
    }
    
    
}
