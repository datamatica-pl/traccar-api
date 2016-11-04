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
import pl.datamatica.traccar.api.dtos.JsonIgnore;
import pl.datamatica.traccar.model.DeviceEvent;

public class AlertDto implements ICachedDto {
    private long id;
    private Date time;
    private String type;
    private long deviceId;
    private Long geofenceId;
    private Long maintenanceId;
    private long positionId;

    public static class Builder {

        private long id;
        private Date time;
        private String type;
        private long deviceId;
        private Long geofenceId;
        private Long maintenanceId;
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

        public Builder maintenanceId(final Long value) {
            this.maintenanceId = value;
            return this;
        }

        public Builder positionId(final long value) {
            this.positionId = value;
            return this;
        }
        
        public Builder event(final DeviceEvent event) {
            this.id = event.getId();
            this.time = event.getTime();
            this.type = event.getType().name();
            this.deviceId = event.getDevice().getId();
            if(event.getGeoFence() != null)
                this.geofenceId = event.getGeoFence().getId();
            if(event.getMaintenance() != null)
                this.maintenanceId = event.getMaintenance().getId();
            this.positionId = event.getPosition().getId();
            return this;
        }

        public AlertDto build() {
            return new AlertDto(id, time, type, deviceId, geofenceId, maintenanceId, positionId);
        }
    }

    private AlertDto(final long id, 
            final Date time, 
            final String type, 
            final long deviceId,
            final Long geofenceId, 
            final Long maintenanceId, 
            final long positionId) {
        this.id = id;
        this.time = time;
        this.type = type;
        this.deviceId = deviceId;
        this.geofenceId = geofenceId;
        this.maintenanceId = maintenanceId;
        this.positionId = positionId;
    }
    
    @Override
    public Date getModificationTime() {
        return time;
    }
}
