/*
 *  Copyright (C) 2018  Datamatica (dev@datamatica.pl)
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
import java.util.Collections;
import java.util.List;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;

public class EditRouteDto extends RouteBaseDto {
    private final List<AddGeoFenceDto> newGeofences;
    private final Float corridorWidth;
    private final Boolean archive;
    private final Boolean cancel;
    
    public EditRouteDto(String name, Long deviceId, int tolerance, int archiveAfter, 
            boolean forceFirst, boolean forceLast, List<RoutePointDto> points, 
            String polyline, List<AddGeoFenceDto> newGeofences, Float corridorWidth,
            Boolean archive, Boolean cancel) {
        super(name, deviceId, tolerance, archiveAfter, forceFirst, forceLast, points, polyline);
        this.newGeofences = newGeofences;
        this.corridorWidth = corridorWidth;
        this.archive = archive;
        this.cancel = cancel;
    }
    
    public List<AddGeoFenceDto> getNewGeofences() {
        return newGeofences;
    }
    
    public Float getCorridorWidth() {
        return corridorWidth;
    }
    
    public Boolean getArchive() {
        return archive;
    }
    
    public Boolean getCancel() {
        return cancel;
    }
    
    public static List<ErrorDto> validate(EditRouteDto dto) {
        if(dto == null)
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_DATA_NOT_PROVIDED));
        
        List<ErrorDto> errors = new ArrayList<>();
        if(dto.getName() == null || dto.getName().isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_ROUTE_NAME_NOT_PROVIDED));
        if(dto.getTolerance() == null)
            errors.add(new ErrorDto(MessageKeys.ERR_ROUTE_TOLERANCE_NOT_PROVIDED));
        if(dto.getForceFirst() == null)
            errors.add(new ErrorDto(MessageKeys.ERR_ROUTE_FORCE_FIRST_NOT_PROVIDED));
        if(dto.getForceLast() == null)
            errors.add(new ErrorDto(MessageKeys.ERR_ROUTE_FORCE_LAST_NOT_PROVIDED));
        if(dto.getArchiveAfter() == null)
            errors.add(new ErrorDto(MessageKeys.ERR_ROUTE_ARCHIVE_AFTER_NOT_PROVIDED));
        if(dto.getPoints() == null)
            errors.add(new ErrorDto(MessageKeys.ERR_ROUTE_POINTS_NOT_PROVIDED));
        else
            for(RoutePointDto rp : dto.getPoints())
                errors.addAll(RoutePointDto.validate(rp));
        if(dto.getPolyline() == null)
            errors.add(new ErrorDto(MessageKeys.ERR_ROUTE_POLYLINE_NOT_PROVIDED));
        for(AddGeoFenceDto gf : dto.getNewGeofences())
            errors.addAll(AddGeoFenceDto.validateRoute(gf));
        return errors;
    }
}
