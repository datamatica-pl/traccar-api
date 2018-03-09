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
package pl.datamatica.traccar.api.dtos.in;

import java.util.Date;
import pl.datamatica.traccar.model.RoutePoint;

/**
 *
 * @author ŁŁ
 */
public class RoutePointDto {
    private final Long id;
    private final Long geofenceId;
    private final Date deadline;
    private final Date enterTime;
    private final Date exitTime;

    public RoutePointDto(long id, long geofenceId, Date deadline, Date enterTime, Date exitTime) {
        this.id = id;
        this.geofenceId = geofenceId;
        this.deadline = deadline;
        this.enterTime = enterTime;
        this.exitTime = exitTime;
    }

    public static class Builder {

        private long id;
        private long geofenceId;
        private Date deadline;
        private Date enterTime;
        private Date exitTime;

        Builder() {
        }

        public Builder id(final long value) {
            this.id = value;
            return this;
        }

        public Builder geofenceId(final long value) {
            this.geofenceId = value;
            return this;
        }

        public Builder deadline(final Date value) {
            this.deadline = value;
            return this;
        }

        public Builder enterTime(final Date value) {
            this.enterTime = value;
            return this;
        }

        public Builder exitTime(final Date value) {
            this.exitTime = value;
            return this;
        }
        
        public Builder routePoint(final RoutePoint rp) {
            return id(rp.getId()).geofenceId(rp.getGeofence().getId())
                    .deadline(rp.getDeadline())
                    .enterTime(rp.getEnterTime()).exitTime(rp.getExitTime());
        }

        public RoutePointDto build() {
            return new RoutePointDto(id, geofenceId, deadline, enterTime, exitTime);
        }
    }

    public static RoutePointDto.Builder builder() {
        return new RoutePointDto.Builder();
    }    
    
}
