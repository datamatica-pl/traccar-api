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

import pl.datamatica.traccar.api.dtos.in.AddGeoFenceDto;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import pl.datamatica.traccar.model.GeoFence;

public class GeoFenceDto extends AddGeoFenceDto implements ICachedDto {
    private final long id;
    private final Date lastUpdate;

    public static class Builder {

        private long id;
        private String geofenceName;
        private String description;
        private boolean allDevices;
        private String color;
        private List<PointDto> points;
        private float radius;
        private String type;
        private long[] deviceIds;
        private Date lastUpdate;

        public Builder geoFence(GeoFence geofence) {
            this.id = geofence.getId();
            this.geofenceName = geofence.getName();
            this.description = geofence.getDescription();
            this.allDevices = geofence.isAllDevices();
            this.color = geofence.getColor();
            if(geofence.getPoints() != null) {
                points = Stream.of(geofence.getPoints().split(","))
                    .map(PointDto::parsePoint)
                    .collect(Collectors.toList());
            }
            this.radius = geofence.getRadius();
            if(geofence.getType() != null)
                this.type = geofence.getType().name();
            if(geofence.getDevices() == null)
                this.deviceIds = new long[0];
            else
                this.deviceIds = geofence.getDevices().stream()
                        .mapToLong(d -> d.getId()).toArray();
            this.lastUpdate = geofence.getLastUpdate();
            return this;
        }

        public GeoFenceDto build() {
            return new GeoFenceDto(id, geofenceName, description, allDevices, color, points, radius, type, deviceIds, lastUpdate);
        }
    }

    public GeoFenceDto(final long id, 
            final String geofenceName, 
            final String description, 
            final boolean allDevices, 
            final String color, 
            final List<PointDto> points, 
            final float radius, 
            final String type, 
            final long[] deviceIds,
            final Date lastUpdate) {
        super(geofenceName, description, allDevices, color, points, radius, type, deviceIds);
        this.id = id;
        this.lastUpdate = lastUpdate;
    }

    public long getId() {
        return id;
    }
    
    @Override
    public Date getModificationTime() {
        return lastUpdate;
    }
}
