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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import pl.datamatica.traccar.model.GeoFence;

public class GeoFenceDto implements ICachedDto {
    private long id;
    private String geofenceName;
    private String description;
    private boolean allDevices;
    private String color;
    private List<PointDto> points;
    private float radius;
    private String type;
    private Date lastUpdate;
    
    public GeoFenceDto(GeoFence gf) {
        this.id = gf.getId();
        this.geofenceName = gf.getName();
        this.description = gf.getDescription();
        this.allDevices = gf.isAllDevices();
        this.color = gf.getColor();
        this.radius = gf.getRadius();
        if(gf.getPoints() != null) {
            points = Stream.of(gf.getPoints().split(","))
                    .map(PointDto::parsePoint)
                    .collect(Collectors.toList());
        }
        if(gf.getType() != null)
            this.type = gf.getType().name();
        this.lastUpdate = gf.getLastUpdate();
        
    }

    @Override
    public Date getModificationTime() {
        return lastUpdate;
    }
}
