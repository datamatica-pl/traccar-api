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
import java.util.List;
import javax.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.SQLDelete;

/**
 *
 * @author Jan Usarek
 */
@Entity
@Table( name = "device_models" )
@PersistenceContext(unitName = "traccar_api_metadata_persistence")
@SQLDelete(sql="UPDATE device_models d SET d.isDeleted = 1 WHERE d.id = ?")
@FilterDef(name="softDelete", defaultCondition="isDeleted = 0")
@Filter(name="softDelete")
public class DeviceModel extends UpdateTimestampedEntity implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;

    @Column(nullable = false, length=100)
    private String modelName;

    @Column(length=500)
    private String imageUrl;
    
    @Column(length=100)
    private String superStatusCommands;

    @Column(length=100)
    private String descriptionShort;

    @Column(length=500)
    private String descriptionLong;

    @Column(length=500)
    private String shopUrl;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted;

    @ManyToMany
    @JoinTable(
        name="devicemodel_commandtype",
        joinColumns=@JoinColumn(name="deviceModelId", referencedColumnName="id"),
        inverseJoinColumns=@JoinColumn(name="commandTypeId", referencedColumnName="id"))
    private List<CommandType> commandTypes;

    public List<CommandType> getCommandTypes() {
        return commandTypes;
    }

    public void setCommandTypes(List<CommandType> commandTypes) {
        this.commandTypes = commandTypes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescriptionShort() {
        return descriptionShort;
    }

    public void setDescriptionShort(String descriptionShort) {
        this.descriptionShort = descriptionShort;
    }

    public String getDescriptionLong() {
        return descriptionLong;
    }

    public void setDescriptionLong(String descriptionLong) {
        this.descriptionLong = descriptionLong;
    }

    public String getShopUrl() {
        return shopUrl;
    }

    public void setShopUrl(String shopUrl) {
        this.shopUrl = shopUrl;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
    
    public String getSuperStatusCommands() {
        return this.superStatusCommands;
    }
}
