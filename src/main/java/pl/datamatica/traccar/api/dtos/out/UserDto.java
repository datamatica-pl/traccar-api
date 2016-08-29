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
    private final long id;
    private final String login;
    private final String email;
    private final String companyName;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final Date expirationDate;
    private final Integer maxNumOfDevices;
    private final Long managedById;
    private final boolean manager;
    private final boolean admin;
    private final boolean archive;
    private final boolean blocked;

    public static class Builder {

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

        public Builder id(final long value) {
            this.id = value;
            return this;
        }

        public Builder login(final String value) {
            this.login = value;
            return this;
        }

        public Builder email(final String value) {
            this.email = value;
            return this;
        }

        public Builder companyName(final String value) {
            this.companyName = value;
            return this;
        }

        public Builder firstName(final String value) {
            this.firstName = value;
            return this;
        }

        public Builder lastName(final String value) {
            this.lastName = value;
            return this;
        }

        public Builder phoneNumber(final String value) {
            this.phoneNumber = value;
            return this;
        }

        public Builder expirationDate(final Date value) {
            this.expirationDate = value;
            return this;
        }

        public Builder maxNumOfDevices(final Integer value) {
            this.maxNumOfDevices = value;
            return this;
        }

        public Builder managedById(final Long value) {
            this.managedById = value;
            return this;
        }

        public Builder manager(final boolean value) {
            this.manager = value;
            return this;
        }

        public Builder admin(final boolean value) {
            this.admin = value;
            return this;
        }

        public Builder archive(final boolean value) {
            this.archive = value;
            return this;
        }

        public Builder blocked(final boolean value) {
            this.blocked = value;
            return this;
        }
        
        public Builder user(final User user) {
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
            return this;
        }

        public UserDto build() {
            return new UserDto(id, login, email, companyName, firstName, lastName, phoneNumber, expirationDate, maxNumOfDevices, managedById, manager, admin, archive, blocked);
        }
    }

    private UserDto(final long id, 
            final String login, 
            final String email, 
            final String companyName, 
            final String firstName, 
            final String lastName, 
            final String phoneNumber, 
            final Date expirationDate, 
            final Integer maxNumOfDevices, 
            final Long managedById, 
            final boolean manager, 
            final boolean admin, 
            final boolean archive, 
            final boolean blocked) {
        this.id = id;
        this.login = login;
        this.email = email;
        this.companyName = companyName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.expirationDate = expirationDate;
        this.maxNumOfDevices = maxNumOfDevices;
        this.managedById = managedById;
        this.manager = manager;
        this.admin = admin;
        this.archive = archive;
        this.blocked = blocked;
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
