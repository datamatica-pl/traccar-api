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

import java.util.Date;
import pl.datamatica.traccar.model.DeviceEvent;
import pl.datamatica.traccar.model.Maintenance;

public class AlertDto implements ICachedDto {
    private final long id;
    private final Date time;
    private final String type;
    private final long deviceId;
    private final Long geofenceId;
    private final Double speed;
    private final MaintenanceDto maintenance;
    private final long positionId;

    public static class Builder {

        private long id;
        private Date time;
        private String type;
        private long deviceId;
        private Long geofenceId;
        private Double speed;
        private MaintenanceDto maintenance;
        private long positionId;

        public Builder() {
        }

        public Builder id(final long value) {
            this.id = value;
            return this;
        }

        public Builder time(final Date value) {
            this.time = value;
            return this;
        }

        public Builder type(final String value) {
            this.type = value;
            return this;
        }

        public Builder deviceId(final long value) {
            this.deviceId = value;
            return this;
        }

        public Builder geofenceId(final Long value) {
            this.geofenceId = value;
            return this;
        }
        
        public Builder speed(final Double value) {
            this.speed = value;
            return this;
        }

        public Builder technicalMaintenance(final Maintenance value) {
            this.maintenance = new MaintenanceDto.Builder()
                    .technicalMaintenance(value).build();
            return this;
        }

        public Builder positionId(final long value) {
            this.positionId = value;
            return this;
        }
        
        public Builder event(final DeviceEvent event) {
            id(event.getId()).time(event.getTime()).type(event.getType().name())
                    .deviceId(event.getDevice().getId());
            if(event.getGeoFence() != null)
                geofenceId(event.getGeoFence().getId());
            if(event.getMaintenance() != null)
                technicalMaintenance(event.getMaintenance());
            if (event.getPosition().getSpeed() != null)
                speed(event.getPosition().getSpeedInKmh());
            return positionId(event.getPosition().getId());
        }

        public AlertDto build() {
            return new AlertDto(id, time, type, deviceId, geofenceId, speed, maintenance, positionId);
        }
    }

    private AlertDto(final long id, 
            final Date time, 
            final String type, 
            final long deviceId,
            final Long geofenceId,
            final Double speed,
            final MaintenanceDto maintenance, 
            final long positionId) {
        this.id = id;
        this.time = time;
        this.type = type;
        this.deviceId = deviceId;
        this.geofenceId = geofenceId;
        this.speed = speed;
        this.maintenance = maintenance;
        this.positionId = positionId;
    }
    
    @Override
    public Date getModificationTime() {
        return time;
    }
}
