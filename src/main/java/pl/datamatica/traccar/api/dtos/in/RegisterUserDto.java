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

public class RegisterUserDto {
    private final String email;
    private final String password;
    private final Boolean checkMarketing;
    private final String imei;
    
    public RegisterUserDto(String email, String password, boolean checkMarketing, String imei) {
        this.email = email;
        this.password = password;
        this.checkMarketing = checkMarketing;
        this.imei = imei;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isCheckMarketing() {
        return checkMarketing;
    }
    
    public String getImei() {
        return imei;
    }
    
    public static List<ErrorDto> validate(RegisterUserDto userDto) {
        if(userDto == null)
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_REGISTRATION_DATA_NOT_PROVIDED));
        
        List<ErrorDto> errors = new ArrayList<>();
        if(userDto.email == null || userDto.email.isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_EMAIL_NOT_PROVIDED));
        if(userDto.password == null || userDto.password.isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_PASSWORD_NOT_PROVIDED));
        if(userDto.checkMarketing == null)
            errors.add(new ErrorDto(MessageKeys.ERR_CHECK_MARKETING_NOT_PROVIDED));
        if(userDto.imei == null || userDto.imei.isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_IMEI_NOT_PROVIDED));
        
        return errors;
    }
}
