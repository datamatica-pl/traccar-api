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
package pl.datamatica.traccar.api.dtos.in;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author piotrkrzeszewski
 */
public class AddUserGroupDto {
    protected final String name;
    protected final Set<UserPermission> permissions;
    
    public AddUserGroupDto(String name, Set<UserPermission> permissions) {
        this.name = name;
        this.permissions = permissions;
    }
    
    public static List<ErrorDto> validate(AddUserGroupDto dto) {
        List<ErrorDto> errors = new ArrayList<>();
        if (dto == null)
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_DATA_NOT_PROVIDED));
        
        if (dto.name == null || dto.name.isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_USER_GROUP_NAME_NOT_PROVIDED));

        if (dto.permissions == null) {
            errors.add(new ErrorDto(MessageKeys.ERR_USER_GROUP_PERMISSIONS_NOT_PROVIDED));
        }
        
        if (dto.getPermissions().stream().anyMatch(d -> d == null))
            errors.add(new ErrorDto(MessageKeys.ERR_USER_GROUP_INVALID_PERMISSION));
        
        

        return errors;
    }
    
    public static class Builder {
        private String name;
        private Set<UserPermission> permissions;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder permissions(Set<UserPermission> permissions) {
            this.permissions = EnumSet.copyOf(permissions);
            return this;
        }
        
        public AddUserGroupDto build() {
            return new AddUserGroupDto(name, permissions);
        }
    }

    public String getName() {
        return name;
    }

    public Set<UserPermission> getPermissions() {
        return permissions;
    }
    
    
}
