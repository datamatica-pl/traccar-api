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
package pl.datamatica.traccar.api.metadata.model;

import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.SQLDelete;

/**
 *
 * @author Jan Usarek
 */
@Entity
@Table( name = "imei_numbers" )
@PersistenceContext(unitName = "traccar_api_metadata_persistence")
@SQLDelete(sql="UPDATE imei_numbers i SET i.isDeleted = 1 WHERE i.id = ?")
@FilterDef(name="softDelete", defaultCondition="isDeleted = 0")
@Filter(name="softDelete")
public class ImeiNumber extends UpdateTimestampedEntity implements Serializable {
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    
    @Column(nullable=false, length=16, unique=true)
    private String imei;
    
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }
    
    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
