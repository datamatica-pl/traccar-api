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

import java.util.ArrayList;
import pl.datamatica.traccar.api.dtos.IGeoFenceInfo;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.dtos.out.PointDto;
import pl.datamatica.traccar.model.GeoFenceType;

public class AddGeoFenceDto implements IGeoFenceInfo {
    private final String geofenceName;
    private final String description;
    private final Boolean allDevices;
    private final String color;
    private final List<PointDto> points;
    private final Float radius;
    private final String type;

    public static class Builder {

        private String geofenceName;
        private String description;
        private Boolean allDevices;
        private String color;
        private List<PointDto> points;
        private Float radius;
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
            final Boolean allDevices, 
            final String color, 
            final List<PointDto> points, 
            final Float radius, 
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
    
    public static List<ErrorDto> validate(AddGeoFenceDto geoFenceDto) {
        if(geoFenceDto == null)
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_GEOFENCE_DATA_NOT_PROVIDED));
        
        List<ErrorDto> errors = new ArrayList<>();
        if(geoFenceDto.allDevices == null)
            errors.add(new ErrorDto(MessageKeys.ERR_GEOFENCE_ALL_DEVICES_NOT_PROVIDED));
        if(geoFenceDto.color == null || geoFenceDto.color.isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_GEOFENCE_COLOR_NOT_PROVIDED));
        else {
            Matcher matcher = Application.COLOR_PATTERN.matcher(geoFenceDto.color);
            if(!matcher.matches())
                errors.add(new ErrorDto(MessageKeys.ERR_INVALID_COLOR_FORMAT));
        }
        if(geoFenceDto.geofenceName == null || geoFenceDto.geofenceName.isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_GEOFENCE_NAME_NOT_PROVIDED));
        if(geoFenceDto.points == null || geoFenceDto.points.isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_GEOFENCE_POINTS_NOT_PROVIDED));
        if(geoFenceDto.getType() == null || geoFenceDto.getType().isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_GEOFENCE_TYPE_NOT_PROVIDED));
        else {
            GeoFenceType type;
            try {
                type = GeoFenceType.valueOf(geoFenceDto.getType());
            } catch(IllegalArgumentException e) {
                errors.add(new ErrorDto(MessageKeys.ERR_INVALID_GEOFENCE_TYPE_FORMAT));
                return errors;
            }
            switch(type) {
                case CIRCLE:
                    if(geoFenceDto.radius == null)
                        errors.add(new ErrorDto(MessageKeys.ERR_GEOFENCE_RADIUS_NOT_PROVIDED));
                    break;
                case LINE:
                    if(geoFenceDto.radius == null)
                        errors.add(new ErrorDto(MessageKeys.ERR_GEOFENCE_RADIUS_NOT_PROVIDED));
                    if(geoFenceDto.points.size() < 2)
                        errors.add(new ErrorDto(MessageKeys.ERR_TOO_FEW_GEOFENCE_POINTS));
                    break;
                case POLYGON:
                    if(geoFenceDto.points.size() < 3)
                        errors.add(new ErrorDto(MessageKeys.ERR_TOO_FEW_GEOFENCE_POINTS));
            }
        }
        return errors;
    }
}
