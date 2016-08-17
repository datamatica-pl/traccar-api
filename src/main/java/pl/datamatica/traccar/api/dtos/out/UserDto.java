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
package pl.datamatica.traccar.api.dtos.out;

import java.util.Date;
import pl.datamatica.traccar.model.User;

public class UserDto {
    private long id;
    private String login;
    private String email;
    private String companyName;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Date expirationDate;
    private Integer maxNumOfDevices;
    private Long managedById;
    private boolean manager;
    private boolean admin;
    private boolean archive;
    private boolean blocked;
    
    public UserDto(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.email = user.getEmail();
        this.companyName = user.getCompanyName();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.phoneNumber = user.getPhoneNumber();
        this.expirationDate = user.getExpirationDate();
        this.maxNumOfDevices = user.getMaxNumOfDevices();
        
        if(user.getManagedBy() != null)
            this.managedById = user.getManagedBy().getId();
        this.manager = user.getManager();
        this.admin = user.getAdmin();
        this.archive = user.isArchive();
        this.blocked = user.isBlocked();
    }

    public long getId() {
        return id;
    }

    public String getLogin() {
        return login;
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

    public Long getManagedById() {
        return managedById;
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
    
    
}
