/*
 *  Copyright (C) 2018  Datamatica (dev@datamatica.pl)
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

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;

public class BleDeviceDto {
    private String modelName;
    private Date creationTimestamp;
    private String phoneModel;
    private String phoneSystem;
    
    public String getModelName() {
        return modelName;
    }
    
    public Date getCreationTimestamp() {
        return creationTimestamp;
    }
    
    public String getPhoneModel() {
        return phoneModel;
    }
    
    public String getPhoneSystem() {
        return phoneSystem;
    }
    
    public static List<ErrorDto> validate(BleDeviceDto dto) {
        List<ErrorDto> errors = new ArrayList<>();
        if (dto == null)
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_DATA_NOT_PROVIDED));
        
        if(dto.modelName == null || dto.modelName.isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_MODEL_NAME_NOT_PROVIDED));
        if(dto.creationTimestamp == null)
            errors.add(new ErrorDto(MessageKeys.ERR_CREATION_TIMESTAMP_NOT_PROVIDED));
        if(dto.phoneModel == null || dto.phoneModel.isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_PHONE_MODEL_NOT_PROVIDED));
        if(dto.phoneSystem == null || dto.phoneSystem.isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_PHONE_SYSTEM_NOT_PROVIDED));
        
        return errors;
    }
}
