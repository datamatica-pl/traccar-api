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
package pl.datamatica.traccar.api.utils;

import com.google.gson.Gson;
import java.util.Map;
import pl.datamatica.traccar.model.Position;

/**
 *
 * @author Jan Usarek
 */
public final class BatteryLevelConverter {
    
    private final static String BATTERY_LEVEL_KEY = "power";
    
    private BatteryLevelConverter() {
    }
    
    public static Integer getBatteryLevelPercent(Position position) {
        Integer percentOfBattery = null;
        Gson gson = new Gson();
        Map<String, Object> other = gson.fromJson(position.getOther(), Map.class);
        
        if (other.containsKey(BATTERY_LEVEL_KEY)) {
            try {
                int originalBatteryLevel = (int)Double.parseDouble(other.get(BATTERY_LEVEL_KEY).toString());
                if (originalBatteryLevel >= 0 && originalBatteryLevel <= 6) {
                    double batteryLevel = originalBatteryLevel * 100 / 6;
                    percentOfBattery = (int) batteryLevel;
                }
            } catch (NumberFormatException e) {
                // Do nothing, if we're unable to get battery level we will not show it.
            }
        }
        
        return percentOfBattery;
    }
    
}
