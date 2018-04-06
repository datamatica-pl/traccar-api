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

import pl.datamatica.traccar.model.AppVersions;

/**
 *
 * @author Jan Usarek
 */
public class AppVersionsDto {
    private final String androidVersion;
    private final String androidRequired;
    private final String iosVersion;
    private final String iosRequired;
    private final String messageKey;
    private final String localizedMessage;
    private final String messageUrl;

    public AppVersionsDto(String androidVersion, String androidRequired, String iosVersion, 
            String iosRequired, String messageKey, String localizedMessage, String messageUrl) {
        this.androidVersion = androidVersion;
        this.androidRequired = androidRequired;
        this.iosVersion = iosVersion;
        this.iosRequired = iosRequired;
        this.messageKey = messageKey;
        this.localizedMessage = localizedMessage;
        this.messageUrl = messageUrl;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public String getAndroidRequired() {
        return androidRequired;
    }

    public String getIosVersion() {
        return iosVersion;
    }

    public String getIosRequired() {
        return iosRequired;
    }

    public String getMessageKey() {
        return messageKey;
    }
    
    public String getLocalizedMessage() {
        return localizedMessage;
    }

    public String getMessageUrl() {
        return messageUrl;
    }

    public static class Builder {
        private String androidVersion;
        private String androidRequired;
        private String iosVersion;
        private String iosRequired;
        private String messageKey;
        private String localizedMessage;
        private String messageUrl;
        
        public Builder() {
        }
        
        public Builder appVersions(final AppVersions appVersions)
        {
            this.androidVersion = appVersions.getAndroidVersion();
            this.androidRequired = appVersions.getAndroidRequiredVersion();
            this.iosVersion = appVersions.getIosVersion();
            this.iosRequired = appVersions.getIosRequiredVersion();
            this.messageKey = appVersions.getMessageKey();
            this.localizedMessage = appVersions.getLocalizedMessage();
            this.messageUrl = appVersions.getMessageUrl();
            return this;
        }
        
        public AppVersionsDto build() {
            return new AppVersionsDto(androidVersion, androidRequired, iosVersion, 
                    iosRequired, messageKey, localizedMessage, messageUrl);
        }
    }
}
