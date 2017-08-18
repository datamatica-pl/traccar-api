/*
 *  Copyright (C) 2017  Datamatica (dev@datamatica.pl)
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
package pl.datamatica.traccar.api.dtos.out;

import java.util.EnumSet;
import java.util.Set;
import pl.datamatica.traccar.model.UserGroup;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author ŁŁ
 */
public class UserGroupDto {
    private final long id;
    private final String name;
    private final Set<UserPermission> permissions;
    
    public UserGroupDto(long id, String name, Set<UserPermission> permissions) {
        this.id = id;
        this.name = name;
        this.permissions = permissions;
    }
    
    public static class Builder {
        private long id;
        private String name;
        private Set<UserPermission> permissions;
        
        public Builder id(long id) {
            this.id = id;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder permissions(Set<UserPermission> permissions) {
            this.permissions = EnumSet.copyOf(permissions);
            return this;
        }
        
        public Builder userGroup(UserGroup group) {
            if(group == null)
                return this;
            this.id = group.getId();
            this.name = group.getName();
            this.permissions = EnumSet.copyOf(group.getPermissions());
            return this;
        }
        
        public UserGroupDto build() {
            return new UserGroupDto(id, name, permissions);
        }
    }
}
