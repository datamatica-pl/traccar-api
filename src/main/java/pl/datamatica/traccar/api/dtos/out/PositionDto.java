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

import com.google.gson.Gson;
import java.util.Date;
import java.util.Map;
import pl.datamatica.traccar.model.Position;

public class PositionDto {
    private long id;
    private Double altitude;
    private Double course;
    private Double speed;
    private double latitude;
    private double longitude;
    private Date deviceTime;
    private Boolean ignition;
    private Double battery;
    private boolean isValid;
    private long deviceId;
    
    public PositionDto(Position position) {
        Gson gson = new Gson();
        Map<String, Object> other = gson.fromJson(position.getOther(), Map.class);
        
        this.id = position.getId();
        this.altitude = position.getAltitude();
        this.course = position.getCourse();
        this.speed = position.getSpeed();
        this.latitude = position.getLatitude();
        this.longitude = position.getLongitude();
        this.deviceTime = position.getTime();
        this.isValid = position.getValid();
        this.deviceId = position.getDevice().getId();
        this.ignition = (Boolean)other.get("ignition");
        this.battery = (Double)other.get("battery");
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Date getDeviceTime() {
        return deviceTime;
    }

    public Double getSpeed() {
        return speed;
    }

    public Boolean isIgnition() {
        return ignition;
    }

    public Double getBattery() {
        return battery;
    }

    public long getId() {
        return id;
    }

    public Double getAltitude() {
        return altitude;
    }

    public Double getCourse() {
        return course;
    }

    public Boolean getIgnition() {
        return ignition;
    }

    public boolean isValid() {
        return isValid;
    }

    public long getDeviceId() {
        return deviceId;
    }
}
