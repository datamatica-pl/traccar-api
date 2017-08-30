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

import java.util.ArrayList;
import pl.datamatica.traccar.api.dtos.in.EditUserDto;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.DeviceEventType;
import pl.datamatica.traccar.model.User;

public class UserDto extends EditUserDto {
    private final Long id;
    private final String login;
    private final Long managedById;
    private final UserSettingsDto settings;
    private final UserGroupDto userGroup;
    private final boolean premium;
    private final String userGroupName;

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
        private boolean readOnly;
        private List<String> notificationEvents = new ArrayList<>();
        private UserSettingsDto settings;
        private UserGroupDto userGroup;
        private boolean premium;
        private String userGroupName;

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
        
        public Builder readOnly(final boolean value) {
            this.readOnly = value;
            return this;
        }
        
        public Builder notificationEvents(final Collection<DeviceEventType> value) {
            notificationEvents.clear();
            if(value == null)
                return this;
            for(DeviceEventType det : value)
                notificationEvents.add(det.name());
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
            notificationEvents(user.getNotificationEvents());
        
            if(user.getManagedBy() != null)
                this.managedById = user.getManagedBy().getId();
            this.manager = user.getManager();
            this.admin = user.getAdmin();
            this.archive = user.isArchive();
            this.blocked = user.isBlocked();
            this.readOnly = user.getReadOnly();
            
            this.premium = false;
            for(Device d : user.getDevices())
                if(d.isValid(new Date())) {
                    this.premium = true;
                    break;
                }
            return this;
        }
        
        public Builder sessionUser(final User user) {
            user(user);
            settings = new UserSettingsDto.Builder().userSettings(user.getUserSettings()).build();
            userGroup = new UserGroupDto.Builder().userGroup(user.getUserGroup()).build();
            return this;
        }
        
        public Builder userGroupName(final String name) {
            this.userGroupName = name;
            return this;
        }

        public UserDto build() {
            return new UserDto(id, login, email, companyName, firstName, lastName, 
                    phoneNumber, expirationDate, maxNumOfDevices, managedById, 
                    manager, admin, archive, blocked, notificationEvents, readOnly,
                    settings, userGroup, premium, userGroupName);
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
            final boolean blocked,
            final List<String> notificationEvents,
            final boolean readOnly,
            final UserSettingsDto settings,
            final UserGroupDto userGroup,
            final boolean premium,
            final String userGroupName) {
        super(email, companyName, firstName, lastName, phoneNumber,
                expirationDate, maxNumOfDevices, manager, admin, archive,
                blocked, PASSWORD_PLACEHOLDER, notificationEvents, readOnly);
        this.id = id;
        this.login = login;
        this.managedById = managedById;
        this.settings = settings;
        this.userGroup = userGroup;
        this.premium = premium;
        this.userGroupName = userGroupName;
    }
    
    public long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }


    public Long getManagedById() {
        return managedById;
    }
    
    public boolean isPremium() {
        return premium;
    }
    
    public String getUserGroupName() {
        return userGroupName;
    }

}
