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
@Table( name = "device_icons" )
@PersistenceContext(unitName = "traccar_api_metadata_persistence")
@SQLDelete(sql="UPDATE device_icons d SET d.isDeleted = 1 WHERE d.id = ?")
@FilterDef(name="softDelete", defaultCondition="isDeleted = 0")
@Filter(name="softDelete")
public class DeviceIcon extends UpdateTimestampedEntity implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    
    @Column(nullable=false, length=500)
    private String iconUrl;
    
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted;
    
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isWithoutFrame;
    
    @Column(nullable = false, columnDefinition = "int default 36")
    private int markerWidth;
    
    @Column(nullable = false, columnDefinition = "int default 48")
    private int markerHeight;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
    
    public boolean isWithoutFrame() {
        return isWithoutFrame;
    }
    
    public void setWithoutFrame(boolean withoutFrame) {
        this.isWithoutFrame = withoutFrame;
    }
    
    public int getMarkerWidth() {
        return markerWidth;
    }
    
    public int getMarkerHeight() {
        return markerHeight;
    }
}
