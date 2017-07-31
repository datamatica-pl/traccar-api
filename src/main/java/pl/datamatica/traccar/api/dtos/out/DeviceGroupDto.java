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
package pl.datamatica.traccar.api.dtos.out;

import pl.datamatica.traccar.api.dtos.in.AddDeviceGroupDto;
import pl.datamatica.traccar.model.Group;

/**
 *
 * @author piotrkrzeszewski
 */
public class DeviceGroupDto extends AddDeviceGroupDto {
    private final long id;
    
    private DeviceGroupDto(long id, String description, String name, Long parent) {
        super(description, name, parent);
        this.id = id;
    }
    
    public static class Builder {
        private long id;
        private String description;
        private String name;
        private Long parent_id = null;
    
        public Builder() {
        }
        
        public Builder id(long value) {
            this.id = value;
            return this;
        }
        
        public Builder description(String value) {
            this.description = value;
            return this;
        }
        
        public Builder name(String value) {
            this.name = value;
            return this;
        }
        
        public Builder parent(long value) {
            this.parent_id = value;
            return this;
        }
        
        public Builder deviceGroup(Group group) {
            this.id = group.getId();
            this.description = group.getDescription();
            this.name = group.getName();
            if (group.getParent() != null) {
                this.parent_id = group.getParent().getId();
            }
            return this;
        }
        
        public DeviceGroupDto build() {
            return new DeviceGroupDto(id, description, name, parent_id);
        }
    }

    public long getId() {
        return id;
    }
}
