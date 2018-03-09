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

import com.vividsolutions.jts.geom.CoordinateSequence;
import java.util.ArrayList;
import java.util.List;
import pl.datamatica.traccar.api.dtos.out.PointDto;
import pl.datamatica.traccar.model.DbRoute;
import pl.datamatica.traccar.model.RoutePoint;

/**
 *
 * @author ŁŁ
 */
public class EditRouteDto {
    private final Long deviceId;
    private final Integer tolerance;
    private final Integer archiveAfter;
    private final Boolean forceFirst;
    private final Boolean forceLast;
    private final List<RoutePointDto> points;
    private final List<PointDto> polyline;

    public EditRouteDto(Long deviceId, 
            int tolerance, 
            int archiveAfter, 
            boolean forceFirst, 
            boolean forceLast, 
            List<RoutePointDto> points, 
            List<PointDto> polyline) {
        this.deviceId = deviceId;
        this.tolerance = tolerance;
        this.archiveAfter = archiveAfter;
        this.forceFirst = forceFirst;
        this.forceLast = forceLast;
        this.points = points;
        this.polyline = polyline;
    }

    public static class Builder {

        protected Long deviceId;
        protected Integer tolerance;
        protected Integer archiveAfter;
        protected Boolean forceFirst;
        protected Boolean forceLast;
        protected List<RoutePointDto> points;
        protected List<PointDto> polyline;

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

        public Builder polyline(final List<PointDto> value) {
            this.polyline = value;
            return this;
        }
        
        public Builder route(final DbRoute route) {
            tolerance(route.getTolerance()).archiveAfter(route.getArchiveAfter())
                    .forceFirst(route.isForceFirst()).forceLast(route.isForceLast());
            if(route.getDevice() != null)
                deviceId(route.getDevice().getId());
            points = new ArrayList<>();
            for(RoutePoint rp : route.getRoutePoints())
                points.add(new RoutePointDto.Builder().routePoint(rp).build());
            polyline = new ArrayList<>();
            if(route.getLineString() != null) {
                CoordinateSequence coords = route.getLineString().getCoordinateSequence();
                for(int i=0;i<coords.size();++i)
                    polyline.add(new PointDto(coords.getX(i),coords.getY(i)));
            }
            return this;
        }

        public EditRouteDto build() {
            return new EditRouteDto(deviceId, tolerance, archiveAfter, forceFirst, forceLast, points, polyline);
        }
    }    
}
