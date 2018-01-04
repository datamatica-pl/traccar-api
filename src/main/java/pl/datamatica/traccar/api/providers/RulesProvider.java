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

import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import pl.datamatica.traccar.api.dtos.in.AddRulesDto;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.model.RulesVersion;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author ŁŁ
 */
public class RulesProvider extends ProviderBase {
    private final User user;
    
    public RulesProvider(EntityManager em, User requestUser) {
        super(em);
        this.user = requestUser;
    }
    
    public RulesVersion createRules(AddRulesDto dto) throws ProviderException {
        if(!user.hasPermission(UserPermission.SERVER_MANAGEMENT))
            throw new ProviderException(Type.ACCESS_DENIED);
        
        Date date = dto.getStartDate();
        em.createQuery("UPDATE RulesVersion SET endDate = :date WHERE endDate IS NULL")
                .setParameter("date", date).executeUpdate();
        RulesVersion rv = new RulesVersion(dto.getUrl(), date);
        em.persist(rv);
        
        return rv;
    }
    
    public void acceptVersion(long id) throws ProviderException {
        RulesVersion rv = em.find(RulesVersion.class, id);
        if(rv == null)
            throw new ProviderException(Type.NOT_FOUND);
        
        user.addRulesAcceptance(rv);
        em.persist(user);
    }
    
    public RulesVersion getActiveRules() {
        List<RulesVersion> v = em.createQuery("from RulesVersion where startDate <= :now AND "
                + "(endDate IS NULL OR endDate > :now)", RulesVersion.class)
                .setParameter("now", new Date())
                .setMaxResults(1)
                .getResultList();
        if(v.isEmpty())
            return null;
        return v.get(0);
    }
}
