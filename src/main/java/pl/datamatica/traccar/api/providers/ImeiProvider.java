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

import java.util.List;
import javax.persistence.*;
import pl.datamatica.traccar.api.metadata.model.ImeiNumber;

/**
 *
 * @author Jan Usarek
 */
public class ImeiProvider {
    private final EntityManager emMetadata;
    
    public ImeiProvider(EntityManager emm) {
        this.emMetadata = emm;
    }
    
    public List<ImeiNumber> getAllImeis() {
        TypedQuery<ImeiNumber> tq = emMetadata.createQuery("Select x from ImeiNumber x", ImeiNumber.class);
        return tq.getResultList();
    }
}
