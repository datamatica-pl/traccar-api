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

/**
 *
 * @author piotrkrzeszewski
 */
public class ApplicationSettingsDto {
    private final long id;
    private final boolean registrationEnabled;
    private final Short updateInterval;
    private final String defaultPasswordHash;
    private final boolean disallowDeviceManagementByUsers;
    private final boolean evantRecordingEnabled;
    private final int notificationExpirationPeriod;
    private final String language;
    private final String bingMapsKey;
    private final String matchServiceURL;
    private final boolean allowCommandsOnlyForAdmins;

    private ApplicationSettingsDto(long id, 
            boolean registrationEnabled, 
            Short updateInterval, 
            String defaultPasswordHash, 
            boolean disallowDeviceManagementByUsers, 
            boolean evantRecordingEnabled, 
            int notificationExpirationPeriod, 
            String language, 
            String bingMapsKey, 
            String matchServiceURL, 
            boolean allowCommandsOnlyForAdmins) {
        this.id = id;
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
 
    public static class Builder {
        private long id;
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
        
        public Builder id(final long value) {
            this.id = value;
            return this;
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
        
        public ApplicationSettingsDto build() {
            return new ApplicationSettingsDto(id, 
                    registrationEnabled, 
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

