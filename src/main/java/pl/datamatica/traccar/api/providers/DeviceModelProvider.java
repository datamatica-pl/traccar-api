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
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.api.metadata.model.DeviceModel;

/**
 *
 * @author Jan Usarek
 */
public class DeviceModelProvider extends ProviderBase {
    private final EntityManager emMetadata;

    public DeviceModelProvider(EntityManager em, EntityManager emm) {
        super(em);
        this.emMetadata = emm;
    }

    public List<DeviceModel> getDeviceModelsMetadata() {
        TypedQuery<DeviceModel> query = emMetadata.createQuery(
                                            "SELECT d FROM DeviceModel d ORDER BY d.updateTime DESC",
                                            DeviceModel.class);
        return query.getResultList();
    }
    
    public DeviceModel getDeviceModel(long id) {
        TypedQuery<DeviceModel> query = emMetadata.createQuery(
                "from DeviceModel d where d.id = :id", DeviceModel.class)
                .setParameter("id", id);
        return query.getSingleResult();
    }
}
