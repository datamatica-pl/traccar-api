/*
 *  Copyright (C) 2018  Datamatica (dev@datamatica.pl)
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
package pl.datamatica.traccar.api.providers;

import java.util.List;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import pl.datamatica.traccar.model.DbRoute;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;
import pl.datamatica.traccar.api.providers.ProviderException.Type;

/**
 *
 * @author ŁŁ
 */
public class RouteProvider extends ProviderBase {
    private final String SELECT_WITH_JOINS = "select distinct r from DbRoute r left join fetch r.device"
            + " left join fetch r.routePoints rp left join fetch rp.geofence"
            + " left join fetch r.corridor";
    
    private final User requestUser;
    
    public RouteProvider(EntityManager em, User requestUser) {
        super(em);
        this.requestUser = requestUser;
    }
    
    public Stream<DbRoute> getAllAvailableRoutes(boolean archive) throws ProviderException {
        if(!requestUser.hasPermission(UserPermission.TRACK_READ))
            throw new ProviderException(Type.ACCESS_DENIED);
        if(requestUser.hasPermission(UserPermission.ALL_TRACKS))
            return em.createQuery(SELECT_WITH_JOINS+" where r.archive = :archive", DbRoute.class)
                    .setParameter("archive", archive).getResultList().stream();
        return em.createQuery(SELECT_WITH_JOINS+" where r.owner = :user and r.archive = :archive", DbRoute.class)
                .setParameter("user", requestUser).setParameter("archive", archive)
                .getResultList().stream();
    }
}
