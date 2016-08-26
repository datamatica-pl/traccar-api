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

public class PositionDto implements ICachedDto {
    private static final String IGNITION_KEY="ignition";
    private static final String BATTERY_KEY = "battery";
    
    private final long id;
    private final Double altitude;
    private final Double course;
    private final Double speed;
    private final double latitude;
    private final double longitude;
    private final Date deviceTime;
    private final Boolean ignition;
    private final Double battery;
    private final boolean isValid;
    private final long deviceId;

    public static class Builder {

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

        public Builder id(final long value) {
            this.id = value;
            return this;
        }

        public Builder altitude(final Double value) {
            this.altitude = value;
            return this;
        }

        public Builder course(final Double value) {
            this.course = value;
            return this;
        }

        public Builder speed(final Double value) {
            this.speed = value;
            return this;
        }

        public Builder latitude(final double value) {
            this.latitude = value;
            return this;
        }

        public Builder longitude(final double value) {
            this.longitude = value;
            return this;
        }

        public Builder deviceTime(final Date value) {
            this.deviceTime = value;
            return this;
        }

        public Builder ignition(final Boolean value) {
            this.ignition = value;
            return this;
        }

        public Builder battery(final Double value) {
            this.battery = value;
            return this;
        }

        public Builder isValid(final boolean value) {
            this.isValid = value;
            return this;
        }

        public Builder deviceId(final long value) {
            this.deviceId = value;
            return this;
        }
        
        public Builder position(final Position position) {
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
            if(other != null) {
                this.ignition = (Boolean)other.get(IGNITION_KEY);
                if(other.containsKey(BATTERY_KEY))
                    this.battery = Double.parseDouble(other.get(BATTERY_KEY).toString());
            }
            return this;
        }

        public PositionDto build() {
            return new PositionDto(id, altitude, course, speed, latitude, longitude, deviceTime, ignition, battery, isValid, deviceId);
        }
    }

    private PositionDto(final long id, 
            final Double altitude, 
            final Double course, 
            final Double speed, 
            final double latitude, 
            final double longitude, 
            final Date deviceTime, 
            final Boolean ignition, 
            final Double battery, 
            final boolean isValid, 
            final long deviceId) {
        this.id = id;
        this.altitude = altitude;
        this.course = course;
        this.speed = speed;
        this.latitude = latitude;
        this.longitude = longitude;
        this.deviceTime = deviceTime;
        this.ignition = ignition;
        this.battery = battery;
        this.isValid = isValid;
        this.deviceId = deviceId;
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

    @Override
    public Date getModificationTime() {
        return getDeviceTime();
    }
}
