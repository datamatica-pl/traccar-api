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

import java.util.Date;
import java.util.List;
import pl.datamatica.traccar.api.dtos.in.EditReportDto;
import pl.datamatica.traccar.model.Report;

/**
 *
 * @author ŁŁ
 */
public class ReportDto extends EditReportDto {
    private long id;

    public static class Builder extends EditReportDto.Builder{

        private long id;

        public Builder() {
        }
        
        public Builder id(final long value) {
            this.id = value;
            return this;
        }
        
        public Builder report(final Report report) {
            super.report(report);
            return id(report.getId());
        }

        public ReportDto build() {
            return new ReportDto(id, name, reportType, deviceIds, geofenceIds, 
                    fromDate, toDate, includeMap, disableFilter);
        }
    }

    protected ReportDto(final long id, final String name, final String reportType, 
            final List<Long> deviceIds, final List<Long> geofenceIds, 
            final Date fromDate, final Date toDate, final boolean includeMap, 
            final boolean disableFilter) {
        super(name, reportType, deviceIds, geofenceIds, fromDate, toDate, includeMap, disableFilter);
        this.id = id;
    }    
}
