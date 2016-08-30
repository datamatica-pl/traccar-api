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

/**
 *
 * @author Jan Usarek
 */
@Entity
@Table( name = "command_parameters" )
@PersistenceContext(unitName = "traccar_api_metadata_persistence")
public class CommandParameter extends TimestampedEntity implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;

    @Column(nullable = false, length=100)
    private String name;

    @Column(nullable = false, length=45)
    private String valueType;

    @Column(length=100)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="commandTypeId")
    private CommandType commandType;
    
    @OneToMany(fetch = FetchType.EAGER, mappedBy="commandParameter")
    private List<CommandConstraint> commandConstraints;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public List<CommandConstraint> getCommandConstraints() {
        return commandConstraints;
    }

    public void setCommandConstraints(List<CommandConstraint> commandConstraints) {
        this.commandConstraints = commandConstraints;
    }
}
