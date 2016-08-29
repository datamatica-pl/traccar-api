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
package pl.datamatica.traccar.api.providers;

import java.util.Collections;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import pl.datamatica.traccar.api.dtos.in.AddGeoFenceDto;
import pl.datamatica.traccar.api.dtos.IGeoFenceInfo;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.GeoFenceType;
import pl.datamatica.traccar.model.User;

public class GeoFenceProvider extends ProviderBase{
    private User requestUser;
    
    public GeoFenceProvider(EntityManager em) {
        super(em);
    }
    
    public void setRequestUser(User user) {
        this.requestUser = user;
    }
    
    public Stream<GeoFence> getAllAvailableGeoFences() {
        return requestUser.getAllAvailableGeoFences().stream();
    }
    
    public GeoFence getGeoFence(long id) throws ProviderException {
        return get(GeoFence.class, id, 
                gf -> requestUser.getAllAvailableGeoFences().contains(gf));
    }

    public GeoFence createGeoFence(IGeoFenceInfo geoFenceDto) {
        GeoFence gf = new GeoFence();
        gf.setName(geoFenceDto.getGeofenceName());
        gf.setDescription(geoFenceDto.getDescription());
        gf.setAllDevices(geoFenceDto.isAllDevices());
        gf.setColor(geoFenceDto.getColor());
        gf.setPoints(geoFenceDto.getPointsString());
        gf.setRadius(geoFenceDto.getRadius());
        gf.setType(GeoFenceType.valueOf(geoFenceDto.getType()));
        gf.setUsers(Collections.singleton(requestUser));
        em.persist(gf);
        
        return gf;
    }
}
