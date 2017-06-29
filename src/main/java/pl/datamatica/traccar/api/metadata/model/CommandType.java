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
@Table( name = "command_types" )
@PersistenceContext(unitName = "traccar_api_metadata_persistence")
public class CommandType extends UpdateTimestampedEntity implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;

    @Column(nullable=false, length=100)
    private String commandName;

    @Column(length=100)
    private String description;
    
    @Column(nullable=false, columnDefinition="boolean default true")
    private boolean isTCP;
    
    @Column(length=300)
    private String commandSMS;
    
    @Column(length=100)
    private String warning;

    @OneToMany(fetch=FetchType.EAGER)
    @JoinColumn(name="CommandTypeId", nullable = false)
    private List<CommandParameter> commandParameters;

    public List<CommandParameter> getCommandParameters() {
        return commandParameters;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isTCP() {
        return isTCP;
    }
    
    public String getCommandSMS() {
        return commandSMS;
    }
    
    public String getWarning() {
        return warning;
    }
}
