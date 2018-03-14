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
package pl.datamatica.traccar.api.dtos.out;

import java.util.Date;
import java.util.List;
import pl.datamatica.traccar.api.dtos.in.RouteBaseDto;
import pl.datamatica.traccar.api.dtos.in.RoutePointDto;
import pl.datamatica.traccar.model.DbRoute;

/**
 *
 * @author ŁŁ
 */
public class RouteDto extends RouteBaseDto {
    private final long id;
    private final Date createdDate;
    private final String status;
    private final Long corridorId;

    public RouteDto(long id, Date createdDate, 
            String status, Long corridorId,
            String name, Long deviceId, int tolerance, 
            int archiveAfter, boolean forceFirst, 
            boolean forceLast, List<RoutePointDto> points, 
            String polyline) {
        super(name, deviceId, tolerance, archiveAfter, forceFirst, forceLast, points, polyline);
        this.id = id;
        this.createdDate = createdDate;
        this.status = status;
        this.corridorId = corridorId;
    }

    public static class Builder extends RouteBaseDto.Builder{

        private long id;
        private Date createdDate;
        private String status;
        private Long corridorId;

        public Builder() {
        }

        public Builder id(final long value) {
            this.id = value;
            return this;
        }

        public Builder createdDate(final Date value) {
            this.createdDate = value;
            return this;
        }

        public Builder status(final String value) {
            this.status = value;
            return this;
        }
        
        public Builder corridorId(final long value) {
            this.corridorId = value;
            return this;
        }
        
        @Override
        public Builder route(DbRoute route) {
            super.route(route);
            if(route.getCorridor() != null)
                corridorId(route.getCorridor().getId());
            return id(route.getId()).createdDate(route.getCreated())
                    .status(route.getStatus().name());
        }

        public RouteDto build() {
            return new RouteDto(id, createdDate, status, corridorId, name, deviceId, 
                    tolerance, archiveAfter, forceFirst, forceLast, points, polyline);
        }
    }    
}
