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
package pl.datamatica.traccar.api.fcm;

import java.util.List;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.model.Device;

public class FcmNotificationDto {
    public static class NotificationData {
        private final String kind;
        
        public NotificationData(String kind) {
            this.kind = kind;
        }
    }
    
    public static class NotificationBody {
        private String body_loc_key;
        private String[] body_loc_args;
        private String icon;
        
        public static NotificationBody subscription(List<Device> devices) {
            NotificationBody body = new NotificationBody();
            
            body.icon = "notification_icon";
            if(devices.size() == 1) {
                body.body_loc_key = MessageKeys.NOTIFICATION_ENDING_SUBSCRIPTION_SINGLE;
                body.body_loc_args = new String[1];
                body.body_loc_args[0] = devices.get(0).getName();
            } else 
                body.body_loc_key = MessageKeys.NOTIFICATION_ENDING_SUBSCRIPTION_MULTIPLE;

            return body;
        }
    }
    
    private NotificationBody notification;
    private NotificationData data;
    private Boolean content_available;
    private String priority = "high";
    private String collapse_key;
    private String to;
    
    public String getTo() {
        return to;
    }
    
    public String getKind() {
        return data.kind;
    }
    
    public static FcmNotificationDto subsciption(List<Device> devices, String to) {
        FcmNotificationDto dto = new FcmNotificationDto();
        dto.notification = NotificationBody.subscription(devices);
        dto.data = new NotificationData("subscription");
        dto.to = to;
        return dto;
    }

    public static FcmNotificationDto alarms(String to) {
        FcmNotificationDto dto = new FcmNotificationDto();
        dto.data = new NotificationData("alarms");
        dto.content_available=true;
        dto.collapse_key = "alarms_key";
        dto.to = to;
        return dto;
    }
}
