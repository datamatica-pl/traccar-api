/*
 *  Copyright (C) 2018  Datamatica (dev@datamatica.pl)
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

import java.util.Date;
import pl.datamatica.traccar.model.RulesVersion;

/**
 *
 * @author ŁŁ
 */
public class RulesDto {
    private final long id;
    private final String url;
    private final Date startDate;
    private final boolean isObligatory;
    private final String description;
    
    public RulesDto(long id, String url, Date startDate, boolean isObligatory, String description) {
        this.id = id;
        this.url = url;
        this.startDate = startDate;
        this.isObligatory = isObligatory;
        this.description = description;
    }
    
    public static class Builder {
        private long id;
        private String url;
        private Date startDate;
        private boolean isObligatory;
        private String description;
        
        public Builder rulesVersion(RulesVersion rv) {
            this.id = rv.getId();
            this.url = rv.getUrl();
            this.startDate = rv.getStartDate();
            this.isObligatory = rv.isObligatory();
            this.description = rv.getType().getName();
            return this;
        }
        
        public RulesDto build() {
            return new RulesDto(id, url, startDate, isObligatory, description);
        }
    }
}
