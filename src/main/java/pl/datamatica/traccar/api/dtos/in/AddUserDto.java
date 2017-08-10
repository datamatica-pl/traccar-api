/*
 *  Copyright (C) 2016  Datamatica (dev@datamatica.pl)
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;

/**
 *
 * @author piotrkrzeszewski
 */
public class AddUserDto extends EditUserDto {
    
    private final String login;
    
    public AddUserDto(String email, String companyName, String firstName, String lastName, String phoneNumber, 
            Date expirationDate, Integer maxNumOfDevices, Boolean manager, Boolean admin, Boolean archive, 
            Boolean blocked, String password, List<String> notificationEvents, Boolean readOnly, String login) {
        super(email, companyName, firstName, lastName, phoneNumber, expirationDate, maxNumOfDevices, 
                manager, admin, archive, blocked, password, notificationEvents, readOnly);
        this.login = login;
    }
    
    public static List<ErrorDto> validate(AddUserDto dto) {
        List<ErrorDto> errors = new ArrayList<>();
        if (dto == null)
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_DATA_NOT_PROVIDED));
        
        if(dto.login == null || dto.login.isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_USER_LOGIN_NOT_PROVIDED));

        // Add validation from super
        EditUserDto.validate(dto).forEach((err) -> {
            errors.add(err);
        });

        return errors;
    }

    public String getLogin() {
        return login;
    }
}
