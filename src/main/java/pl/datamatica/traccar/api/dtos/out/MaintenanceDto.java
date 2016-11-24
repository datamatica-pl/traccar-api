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

import java.util.Date;
import pl.datamatica.traccar.model.Maintenance;
import pl.datamatica.traccar.model.MaintenanceBase;
import pl.datamatica.traccar.model.RegistrationMaintenance;

public class MaintenanceDto {
    private long id;
    private String name;
    private Double serviceInterval;
    private Double lastService;
    private Date serviceDate;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getServiceInterval() {
        return serviceInterval;
    }

    public Double getLastService() {
        return lastService;
    }

    public Date getServiceDate() {
        return serviceDate;
    }

    public static class Builder {
        private long id;
        private String name;
        private Double serviceInterval;
        private Double lastService;
        private Date serviceDate;

        public Builder() {
        }

        public Builder id(final long value) {
            this.id = value;
            return this;
        }

        public Builder name(final String value) {
            this.name = value;
            return this;
        }

        public Builder serviceInterval(final Double value) {
            this.serviceInterval = value;
            return this;
        }

        public Builder lastService(final Double value) {
            this.lastService = value;
            return this;
        }

        public Builder serviceDate(final Date value) {
            this.serviceDate = value;
            return this;
        }
        
        private Builder maintenance(MaintenanceBase mb) {
            return id(mb.getId()).name(mb.getName());
        }
        
        public Builder registrationMaintenance(RegistrationMaintenance rm) {
            return maintenance(rm).serviceDate(rm.getServiceDate());
        }
        
        public Builder technicalMaintenance(Maintenance m) {
            return maintenance(m).lastService(m.getLastService())
                    .serviceInterval(m.getServiceInterval());
        }

        public MaintenanceDto build() {
            return new MaintenanceDto(id, name, serviceInterval, lastService, serviceDate);
        }
    }

    private MaintenanceDto(final long id, 
            final String name, 
            final Double serviceInterval, 
            final Double lastService, 
            final Date serviceDate) {
        this.id = id;
        this.name = name;
        this.serviceInterval = serviceInterval;
        this.lastService = lastService;
        this.serviceDate = serviceDate;
    }
    
    
    
}
