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

import com.vividsolutions.jts.geom.Coordinate;
import java.util.ArrayList;
import java.util.List;
import pl.datamatica.traccar.api.reports.PolylineEncoder;
import pl.datamatica.traccar.model.DbRoute;
import pl.datamatica.traccar.model.RoutePoint;

/**
 *
 * @author ŁŁ
 */
public class EditRouteDto {
    private final String name;
    private final Long deviceId;
    private final Integer tolerance;
    private final Integer archiveAfter;
    private final Boolean forceFirst;
    private final Boolean forceLast;
    private final List<RoutePointDto> points;
    private final String polyline;

    public EditRouteDto(String name,
            Long deviceId, 
            int tolerance, 
            int archiveAfter, 
            boolean forceFirst, 
            boolean forceLast, 
            List<RoutePointDto> points, 
            String polyline) {
        this.name = name;
        this.deviceId = deviceId;
        this.tolerance = tolerance;
        this.archiveAfter = archiveAfter;
        this.forceFirst = forceFirst;
        this.forceLast = forceLast;
        this.points = points;
        this.polyline = polyline;
    }

    public static class Builder {

        protected String name;
        protected Long deviceId;
        protected Integer tolerance;
        protected Integer archiveAfter;
        protected Boolean forceFirst;
        protected Boolean forceLast;
        protected List<RoutePointDto> points;
        protected String polyline;

        public Builder() {
        }

        public Builder deviceId(final Long value) {
            this.deviceId = value;
            return this;
        }

        public Builder tolerance(final Integer value) {
            this.tolerance = value;
            return this;
        }

        public Builder archiveAfter(final Integer value) {
            this.archiveAfter = value;
            return this;
        }

        public Builder forceFirst(final Boolean value) {
            this.forceFirst = value;
            return this;
        }

        public Builder forceLast(final Boolean value) {
            this.forceLast = value;
            return this;
        }

        public Builder points(final List<RoutePointDto> value) {
            this.points = value;
            return this;
        }

        public Builder polyline(final String value) {
            this.polyline = value;
            return this;
        }
        
        public Builder name(final String value) {
            this.name = value;
            return this;
        }
        
        public Builder route(final DbRoute route) {
            name(route.getName());
            tolerance(route.getTolerance()).archiveAfter(route.getArchiveAfter())
                    .forceFirst(route.isForceFirst()).forceLast(route.isForceLast());
            if(route.getDevice() != null)
                deviceId(route.getDevice().getId());
            points = new ArrayList<>();
            for(RoutePoint rp : route.getRoutePoints())
                points.add(new RoutePointDto.Builder().routePoint(rp).build());
            polyline = route.getLinePoints();
            return this;
        }

        public EditRouteDto build() {
            return new EditRouteDto(name, deviceId, tolerance, archiveAfter, 
                    forceFirst, forceLast, points, polyline);
        }
    }    
}
