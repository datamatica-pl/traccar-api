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

import pl.datamatica.traccar.api.dtos.in.EditApplicationSettingsDto;
import pl.datamatica.traccar.model.ApplicationSettings;

/**
 *
 * @author piotrkrzeszewski
 */
public class ApplicationSettingsDto extends EditApplicationSettingsDto {
    private final long id;

    private ApplicationSettingsDto(long id, 
            boolean registrationEnabled, 
            Short updateInterval, 
            String defaultPasswordHash, 
            boolean evantRecordingEnabled, 
            int notificationExpirationPeriod, 
            String language, 
            String bingMapsKey, 
            String matchServiceURL, 
            long defaultUserGroupId,
            int defaultIconId) {
        super(registrationEnabled, 
                    updateInterval, 
                    defaultPasswordHash, 
                    evantRecordingEnabled, 
                    notificationExpirationPeriod, 
                    language, 
                    bingMapsKey, 
                    matchServiceURL, 
                    defaultUserGroupId,
                    defaultIconId);
        this.id = id;
    }
    
    public static class Builder {
        private long id;
        private boolean registrationEnabled;
        private Short updateInterval;
        private String defaultPasswordHash;
        private boolean evantRecordingEnabled;
        private int notificationExpirationPeriod;
        private String language;
        private String bingMapsKey;
        private String matchServiceURL;
        private long defaultUserGroupId;
        private int defaultIconId;
        
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
        
        public Builder defaultUserGroupId(final long value) {
            this.defaultUserGroupId = value;
            return this;
        }
        
        public Builder defaultIconId(int value) {
            this.defaultIconId = value;
            return this;
        }
        
        public Builder purgeConfidentialData() {
            this.bingMapsKey = null;
            this.matchServiceURL = null;
            return this;
        }
        
        public Builder applicationSettings(final ApplicationSettings as) {
            this.id = as.getId();
            this.registrationEnabled = as.getRegistrationEnabled();
            this.updateInterval = as.getUpdateInterval();
            this.defaultPasswordHash = as.getDefaultHashImplementation().getName();
            this.evantRecordingEnabled = as.isEventRecordingEnabled();
            this.notificationExpirationPeriod = as.getNotificationExpirationPeriod();
            this.language = as.getLanguage();
            this.bingMapsKey = as.getBingMapsKey();
            this.matchServiceURL = as.getMatchServiceURL();
            this.defaultUserGroupId = as.getDefaultGroup().getId();
            this.defaultIconId = as.getDefaultIconId();
            return this;
        }
        
        public ApplicationSettingsDto build() {
            return new ApplicationSettingsDto(id, 
                    registrationEnabled, 
                    updateInterval, 
                    defaultPasswordHash, 
                    evantRecordingEnabled, 
                    notificationExpirationPeriod, 
                    language, 
                    bingMapsKey, 
                    matchServiceURL, 
                    defaultUserGroupId,
                    defaultIconId);
        }
    }
}

