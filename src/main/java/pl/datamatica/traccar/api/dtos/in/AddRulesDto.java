/*
 *  Copyright (C) 2018  Datamatica (dev@datamatica.pl)
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *  
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *  
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pl.datamatica.traccar.api.dtos.in;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;

/**
 *
 * @author ŁŁ
 */
public class AddRulesDto {
    private final String url;
    private final Date startDate;
    private final String type;
    
    public AddRulesDto(String url, Date startDate, String type) {
        this.url = url;
        this.startDate = startDate;
        this.type = type;
    }
    
    public String getUrl() {
        return url;
    }
    
    public Date getStartDate() {
        return startDate;
    }
    
    public String getType() {
        return type;
    }
    
    public static List<ErrorDto> validate(AddRulesDto dto) {
        if(dto == null)
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_DATA_NOT_PROVIDED));
        if(dto.url == null || dto.url.isEmpty())
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_DATA_NOT_PROVIDED));
        if(dto.startDate == null)
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_DATA_NOT_PROVIDED));
        if(dto.type == null || dto.type.isEmpty())
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_DATA_NOT_PROVIDED));
        return Collections.<ErrorDto>emptyList();
    }
}
