/*
 *  Copyright (C) 2017 Datamatica (dev@datamatica.pl)
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

import java.util.Date;
import pl.datamatica.traccar.model.AuditLog;
import pl.datamatica.traccar.model.AuditLogType;

/**
 *
 * @author piotrkrzeszewski
 */
public class AuditLogDto {
    private final long id;
    private final String note;
    private final Date time;
    private final String agentLogin;
    private final String type;

    public AuditLogDto(long id, String note, Date time, String agentLogin, AuditLogType type) {
        this.id = id;
        this.note = note;
        this.time = time;
        this.agentLogin = agentLogin;
        this.type = type.name();
    }
    
    public static class Builder {
        private long id;
        private String note;
        private Date time;
        private String agentLogin;
        private AuditLogType type;

        public Builder id(long value) {
            this.id = value;
            return this;
        }

        public Builder note(String value) {
            this.note = value;
            return this;
        }

        public Builder time(Date value) {
            this.time = value;
            return this;
        }

        public Builder agentLogin(String value) {
            this.agentLogin = value;
            return this;
        }
        
        public Builder type(AuditLogType value) {
            this.type = value;
            return this;
        }
        
        public Builder auditLog(AuditLog value) {
            this.id = value.getId();
            this.agentLogin = value.getAgentLogin();
            this.time = value.getTime();
            this.type = value.getType();
            this.note = AuditLogDto.generateNoteFromAuditLog(value);
            return this;
        }
        
        public AuditLogDto build() {
            return new AuditLogDto(id, note, time, agentLogin, type);
        }
    }
    
    public static String generateNoteFromAuditLog(AuditLog log) {
        switch(log.getType()) {
            case CHANGED_SERVER_SETTING:
                return "Changed server setting '" + log.getFieldName() + "' to value: " + log.getFieldNewValue();
            case CHANGED_USER:
                return "Changed user " + log.getTargetUserLogin() + " '" + log.getFieldName() + "' to value: " + log.getFieldNewValue();
            case ADDED_USERGROUP_PERMISSION:
                return "Added " + log.getPermissionName() + " permission to usergroup " + log.getTargetUserGroupName();
            case REMOVED_USERGROUP_PERMISSION: 
                return "Removed " + log.getPermissionName() + " permission from usergroup " + log.getTargetUserGroupName();
            case CHANGED_USERGROUP_PROPERTY:
                return "Changed usergroup " + log.getTargetUserGroupName()+ " '" + log.getFieldName() + "' to value: " + log.getFieldNewValue();
            case CHANGED_USER_USERGROUP:
                return "Moved user " + log.getTargetUserLogin()+ " to usergroup: " + log.getTargetUserGroupName();
            case CREATED_USERGROUP:
                return "Created usergroup " + log.getTargetUserGroupName();
            case REMOVED_USERGROUP:
                return "Removed usergroup " + log.getTargetUserGroupName();
            case CREATED_USER:
                return "Created user " + log.getTargetUserLogin();
            case REMOVED_USER:
                return "Removed user " + log.getTargetUserLogin();
        }
        return "Enexpected event";
    }
}
