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

public class NotificationTokenDto {
    private final String token;
    
    public NotificationTokenDto(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
    
    public static List<ErrorDto> validate(NotificationTokenDto tokenDto) {
        if(tokenDto == null || tokenDto.getToken() == null)
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_TOKEN_NOT_PROVIDED));
        return Collections.EMPTY_LIST;
    }
}
