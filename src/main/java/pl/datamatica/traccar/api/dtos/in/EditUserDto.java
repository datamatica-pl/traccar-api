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
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import static pl.datamatica.traccar.api.dtos.MessageKeys.*;

/**
 *
 * @author ŁŁ
 */
public class EditUserDto {
    public static final String PASSWORD_PLACEHOLDER = "******";
    
    public static List<ErrorDto> validate(EditUserDto dto) {
        List<ErrorDto> errors = new ArrayList<>();
        if (dto == null)
            return Collections.singletonList(new ErrorDto(ERR_DATA_NOT_PROVIDED));
        if(dto.manager == null)
            errors.add(new ErrorDto(ERR_USER_MANAGER_NOT_PROVIDED));
        if(dto.admin == null)
            errors.add(new ErrorDto(ERR_USER_ADMIN_NOT_PROVIDED));
        if(dto.archive == null)
            errors.add(new ErrorDto(ERR_USER_ARCHIVE_NOT_PROVIDED));
        if(dto.readOnly == null)
            errors.add(new ErrorDto(ERR_USER_READONLY_NOT_PROVIDED));
        if(dto.password == null || dto.password.isEmpty())
            errors.add(new ErrorDto(ERR_USER_PASSWORD_NOT_PROVIDED));
        if(dto.notificationEvents == null)
            errors.add(new ErrorDto(ERR_USER_NOTIFICATION_EVENTS_NOT_PROVIDED));
        return errors;
    }
    protected final String email;
    protected final String companyName;
    protected final String firstName;
    protected final String lastName;
    protected final String phoneNumber;
    protected final Date expirationDate;
    protected final Integer maxNumOfDevices;
    protected final Boolean manager;
    protected final Boolean admin;
    protected final Boolean archive;
    protected final Boolean blocked;
    protected final Boolean readOnly;
    protected final String password;
    protected final List<String> notificationEvents;

    public EditUserDto(String email, String companyName, String firstName, String lastName, 
            String phoneNumber, Date expirationDate, Integer maxNumOfDevices, 
            Boolean manager, Boolean admin, Boolean archive, Boolean blocked, 
            String password, List<String> notificationEvents, Boolean readOnly) {
        this.email = email;
        this.companyName = companyName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.expirationDate = expirationDate;
        this.maxNumOfDevices = maxNumOfDevices;
        this.manager = manager;
        this.admin = admin;
        this.archive = archive;
        this.blocked = blocked;
        this.password = password;
        this.notificationEvents = notificationEvents;
        this.readOnly = readOnly;
    }
    
    public String getEmail() {
        return email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public Integer getMaxNumOfDevices() {
        return maxNumOfDevices;
    }

    public boolean isManager() {
        return manager;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isArchive() {
        return archive;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public String getPassword() {
        return password;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public List<String> getNotificationEvents() {
        return notificationEvents;
    }    
}
