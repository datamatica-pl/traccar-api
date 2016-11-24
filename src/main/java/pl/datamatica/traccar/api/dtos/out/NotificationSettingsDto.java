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
import java.util.List;
import java.util.Map;
import pl.datamatica.traccar.model.MobNotificationMode;
import pl.datamatica.traccar.model.MobNotificationType;
import static pl.datamatica.traccar.model.MobNotificationType.*;
import static pl.datamatica.traccar.api.dtos.MessageKeys.*;

public class NotificationSettingsDto {

    public static final String NONE = "NONE";
    
    public static List<ErrorDto> validate(NotificationSettingsDto dto) {
        List<ErrorDto> errors = new ArrayList<>();
        
        if(dto.geofence == null || dto.geofence.isEmpty())
            errors.add(new ErrorDto(ERR_GEO_MODE_NOT_PROVIDED));
        else if(!testField(dto.geofence))
            errors.add(new ErrorDto(ERR_INVALID_GEO_MODE));
        
        if(dto.overspeed == null || dto.overspeed.isEmpty())
            errors.add(new ErrorDto(ERR_OSPD_MODE_NOT_PROVIDED));
        else if(!testField(dto.overspeed))
            errors.add(new ErrorDto(ERR_INVALID_OSPD_MODE));
        
        if(dto.policy == null || dto.policy.isEmpty())
            errors.add(new ErrorDto(ERR_POLICY_MODE_NOT_PROVIDED));
        else if(!testField(dto.policy))
            errors.add(new ErrorDto(ERR_INVALID_POLICY_MODE));
        
        if(dto.maintenance == null || dto.maintenance.isEmpty())
            errors.add(new ErrorDto(ERR_MTNCE_MODE_NOT_PROVIDED));
        else if(!testField(dto.maintenance))
            errors.add(new ErrorDto(ERR_INVALID_MTNCE_MODE));
        
        return errors;
    }
    
    private static boolean testField(String field) {
        if(!field.equals(NONE))
            try {
                MobNotificationMode.valueOf(field);
            } catch(IllegalArgumentException e) {
                return false;
            }
        return true;
    }
    
    
    private String geofence;
    private String overspeed;
    private String policy;
    private String maintenance;
    
    public NotificationSettingsDto(Map<MobNotificationType, MobNotificationMode> settings) {
        if(settings.get(GEOFENCE) == null)
            geofence = "NONE";
        else
            geofence = settings.get(GEOFENCE).name();
        
        if(settings.get(OVERSPEED) == null)
            overspeed = "NONE";
        else
            overspeed = settings.get(OVERSPEED).name();
        
        if(settings.get(POLICY) == null)
            policy = "NONE";
        else
            policy = settings.get(POLICY).name();
        
        if(settings.get(MAINTENANCE) == null)
            maintenance = "NONE";
        else
            maintenance = settings.get(MAINTENANCE).name();
    }

    public String getGeofence() {
        return geofence;
    }

    public String getOverspeed() {
        return overspeed;
    }

    public String getPolicy() {
        return policy;
    }

    public String getMaintenance() {
        return maintenance;
    }
}
