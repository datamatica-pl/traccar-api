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
import pl.datamatica.traccar.model.PasswordHashMethod;

/**
 *
 * @author piotrkrzeszewski
 */
public class EditApplicationSettingsDto {
    private final Boolean registrationEnabled;
    private final Short updateInterval;
    private final String defaultPasswordHash;
    private final Boolean disallowDeviceManagementByUsers;
    private final Boolean evantRecordingEnabled;
    private final Integer notificationExpirationPeriod;
    private final String language;
    private final String bingMapsKey;
    private final String matchServiceURL;
    private final Boolean allowCommandsOnlyForAdmins;

    protected EditApplicationSettingsDto(boolean registrationEnabled, 
            Short updateInterval, 
            String defaultPasswordHash, 
            boolean disallowDeviceManagementByUsers, 
            boolean evantRecordingEnabled, 
            int notificationExpirationPeriod, 
            String language, 
            String bingMapsKey, 
            String matchServiceURL, 
            boolean allowCommandsOnlyForAdmins) {
        this.registrationEnabled = registrationEnabled;
        this.updateInterval = updateInterval;
        this.defaultPasswordHash = defaultPasswordHash;
        this.disallowDeviceManagementByUsers = disallowDeviceManagementByUsers;
        this.evantRecordingEnabled = evantRecordingEnabled;
        this.notificationExpirationPeriod = notificationExpirationPeriod;
        this.language = language;
        this.bingMapsKey = bingMapsKey;
        this.matchServiceURL = matchServiceURL;
        this.allowCommandsOnlyForAdmins = allowCommandsOnlyForAdmins;
    }
 
    public boolean isRegistrationEnabled() {
        return registrationEnabled;
    }

    public Short getUpdateInterval() {
        return updateInterval;
    }

    public String getDefaultPasswordHash() {
        return defaultPasswordHash;
    }

    public boolean isDisallowDeviceManagementByUsers() {
        return disallowDeviceManagementByUsers;
    }

    public boolean isEvantRecordingEnabled() {
        return evantRecordingEnabled;
    }

    public int getNotificationExpirationPeriod() {
        return notificationExpirationPeriod;
    }

    public String getLanguage() {
        return language;
    }

    public String getBingMapsKey() {
        return bingMapsKey;
    }

    public String getMatchServiceURL() {
        return matchServiceURL;
    }

    public boolean isAllowCommandsOnlyForAdmins() {
        return allowCommandsOnlyForAdmins;
    }
    
    public static List<ErrorDto> validate(EditApplicationSettingsDto dto) {
        if(dto == null)
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_APPSETTINGS_DATA_NOT_PROVIDED));
        
        List<ErrorDto> errors = new ArrayList<>();
        if (dto.registrationEnabled == null) {
            errors.add(new ErrorDto(MessageKeys.ERR_APPSETTINGS_REGISTRATION_ENABLED_NOT_PROVIDED));
        }
        if (dto.updateInterval == null) {
            errors.add(new ErrorDto(MessageKeys.ERR_APPSETTINGS_UPDATED_INTERVAL_NOT_PROVIDED));
        }
        if (dto.disallowDeviceManagementByUsers == null) {
            errors.add(new ErrorDto(MessageKeys.ERR_APPSETTINGS_DISALLOW_DEVICE_MANAGEMENT_NOT_PROVIDED));
        }
        if (dto.evantRecordingEnabled == null) {
            errors.add(new ErrorDto(MessageKeys.ERR_APPSETTINGS_EVENT_RECORDING_NOT_PROVIDED));
        }
        if (dto.notificationExpirationPeriod == null) {
            errors.add(new ErrorDto(MessageKeys.ERR_APPSETTINGS_NOTIFICATION_EXPIRATION_PERIOD_NOT_PROVIDED));
        }
        if (dto.language == null || dto.language.isEmpty()) {
            errors.add(new ErrorDto(MessageKeys.ERR_APPSETTINGS_LANGUAGE_NOT_PROVIDED));
        }
        if (dto.allowCommandsOnlyForAdmins == null) {
            errors.add(new ErrorDto(MessageKeys.ERR_APPSETTINGS_ALLOW_COMMANDS_ONLY_FOR_ADMINS_NOT_PROVIDED));
        }
        if (dto.defaultPasswordHash == null || dto.defaultPasswordHash.isEmpty()) {
            errors.add(new ErrorDto(MessageKeys.ERR_APPSETTINGS_PASSWORD_HASH_METHOD_NOT_PROVIDED));
        }
        else { 
            if (PasswordHashMethod.fromString(dto.defaultPasswordHash) == null) {
                errors.add(new ErrorDto(MessageKeys.ERR_APPSETTINGS_ILLEGAL_PASSWORD_HASH_METHOD));
            }
        }
        
        return errors;
    }
    
    public static class Builder {
        private boolean registrationEnabled;
        private Short updateInterval;
        private String defaultPasswordHash;
        private boolean disallowDeviceManagementByUsers;
        private boolean evantRecordingEnabled;
        private int notificationExpirationPeriod;
        private String language;
        private String bingMapsKey;
        private String matchServiceURL;
        private boolean allowCommandsOnlyForAdmins;
        
        public Builder() {
        }
        
        public Builder registrationEnabled(final boolean value) {
            this.registrationEnabled = value;
            return this;
        }
        
        public Builder updateInterval(final Short value) {
            this.updateInterval = value;
            return this;
        }
        
        public Builder defaultPasswordHash(final String value) {
            this.defaultPasswordHash = value;
            return this;
        }
        
        public Builder disallowDeviceManagementByUsers(final boolean value) {
            this.disallowDeviceManagementByUsers = value;
            return this;
        }
        
        public Builder evantRecordingEnabled(final boolean value) {
            this.evantRecordingEnabled = value;
            return this;
        }
        
        public Builder notificationExpirationPeriod(final int value) {
            this.notificationExpirationPeriod = value;
            return this;
        }
        
        public Builder language(final String value) {
            this.language = value;
            return this;
        }
        
        public Builder bingMapsKey(final String value) {
            this.bingMapsKey = value;
            return this;
        }
        
        public Builder matchServiceURL(final String value) {
            this.matchServiceURL = value;
            return this;
        }
        
        public Builder allowCommandsOnlyForAdmins(final boolean value) {
            this.allowCommandsOnlyForAdmins = value;
            return this;
        }
        
        public EditApplicationSettingsDto build() {
            return new EditApplicationSettingsDto(registrationEnabled, 
                    updateInterval, 
                    defaultPasswordHash, 
                    disallowDeviceManagementByUsers, 
                    evantRecordingEnabled, 
                    notificationExpirationPeriod, 
                    language, 
                    bingMapsKey, 
                    matchServiceURL, 
                    allowCommandsOnlyForAdmins);
        }
    }
}

