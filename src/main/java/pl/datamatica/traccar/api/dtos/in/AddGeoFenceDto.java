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
package pl.datamatica.traccar.api.dtos.in;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.dtos.out.PointDto;

public class AddGeoFenceDto implements IGeoFenceInfo {
    private final String geofenceName;
    private final String description;
    private final boolean allDevices;
    private final String color;
    private final List<PointDto> points;
    private final float radius;
    private final String type;

    public static class Builder {

        private String geofenceName;
        private String description;
        private boolean allDevices;
        private String color;
        private List<PointDto> points;
        private float radius;
        private String type;

        public Builder geofenceName(final String value) {
            this.geofenceName = value;
            return this;
        }

        public Builder description(final String value) {
            this.description = value;
            return this;
        }

        public Builder allDevices(final boolean value) {
            this.allDevices = value;
            return this;
        }

        public Builder color(final String value) {
            this.color = value;
            return this;
        }

        public Builder points(final List<PointDto> value) {
            this.points = value;
            return this;
        }

        public Builder radius(final float value) {
            this.radius = value;
            return this;
        }

        public Builder type(final String value) {
            this.type = value;
            return this;
        }

        public AddGeoFenceDto build() {
            return new AddGeoFenceDto(geofenceName, description, allDevices, color, points, radius, type);
        }
    }

    protected AddGeoFenceDto(final String geofenceName, 
            final String description, 
            final boolean allDevices, 
            final String color, 
            final List<PointDto> points, 
            final float radius, 
            final String type) {
        this.geofenceName = geofenceName;
        this.description = description;
        this.allDevices = allDevices;
        this.color = color;
        this.points = points;
        this.radius = radius;
        this.type = type;
    }

    @Override
    public String getGeofenceName() {
        return geofenceName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isAllDevices() {
        return allDevices;
    }

    @Override
    public String getColor() {
        return color;
    }

    public List<PointDto> getPoints() {
        return points;
    }
    
    @Override
    public String getPointsString() {
        return points.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    @Override
    public float getRadius() {
        return radius;
    }

    @Override
    public String getType() {
        return type;
    }    
    
    public static List<ErrorDto> validate(IGeoFenceInfo geoFenceDto) {
        //todo 2016-08-26
        return Collections.EMPTY_LIST;
    }
}
