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
package pl.datamatica.traccar.api.dtos;

import java.util.Date;
import pl.datamatica.traccar.model.Position;

public class PositionDto {
    private double latitude;
    private double longitude;
    private Date time;
    private double speed;
    private boolean ignition;
    private int battery;
    
    public PositionDto(Position position) {
        this.latitude = position.getLatitude();
        this.longitude = position.getLongitude();
        this.time = position.getTime();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Date getTime() {
        return time;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isIgnition() {
        return ignition;
    }

    public int getBattery() {
        return battery;
    }
    
    
}
