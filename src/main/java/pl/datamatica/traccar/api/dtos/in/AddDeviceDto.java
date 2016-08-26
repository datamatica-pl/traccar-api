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

import java.util.Collections;
import java.util.List;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;

public class AddDeviceDto {
    private String imei;
    
    public AddDeviceDto(String imei) {
        this.imei = imei;
    }

    public AddDeviceDto() {
    }
    
    public String getImei() {
        return imei;
    }
    
    public static List<ErrorDto> validate(AddDeviceDto deviceDto) {
        if(deviceDto == null || deviceDto.getImei() == null)
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_IMEI_NOT_PROVIDED));
        return Collections.EMPTY_LIST;
    }
}
