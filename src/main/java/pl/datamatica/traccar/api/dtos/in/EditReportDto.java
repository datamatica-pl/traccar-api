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
package pl.datamatica.traccar.api.dtos.in;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import pl.datamatica.traccar.model.Report;

/**
 *
 * @author ŁŁ
 */
public class EditReportDto {
    private String name;
    private String reportType;
    private List<Long> deviceIds;
    private List<Long> geofenceIds;
    private Date fromDate;
    private Date toDate;
    private boolean includeMap;
    private boolean disableFilter;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public List<Long> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(List<Long> deviceIds) {
        this.deviceIds = deviceIds;
    }

    public List<Long> getGeofenceIds() {
        return geofenceIds;
    }

    public void setGeofenceIds(List<Long> geofenceIds) {
        this.geofenceIds = geofenceIds;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public boolean isIncludeMap() {
        return includeMap;
    }

    public void setIncludeMap(boolean includeMap) {
        this.includeMap = includeMap;
    }

    public boolean isDisableFilter() {
        return disableFilter;
    }

    public void setDisableFilter(boolean disableFilter) {
        this.disableFilter = disableFilter;
    }

    public static class Builder {

        protected String name;
        protected String reportType;
        protected List<Long> deviceIds;
        protected List<Long> geofenceIds;
        protected Date fromDate;
        protected Date toDate;
        protected boolean includeMap;
        protected boolean disableFilter;

        public Builder() {
        }

        public Builder name(final String value) {
            this.name = value;
            return this;
        }

        public Builder reportType(final String value) {
            this.reportType = value;
            return this;
        }

        public Builder deviceIds(final List<Long> value) {
            this.deviceIds = value;
            return this;
        }

        public Builder geofenceIds(final List<Long> value) {
            this.geofenceIds = value;
            return this;
        }

        public Builder fromDate(final Date value) {
            this.fromDate = value;
            return this;
        }

        public Builder toDate(final Date value) {
            this.toDate = value;
            return this;
        }

        public Builder includeMap(final boolean value) {
            this.includeMap = value;
            return this;
        }

        public Builder disableFilter(final boolean value) {
            this.disableFilter = value;
            return this;
        }
        
        public Builder report(final Report report) {
            name(report.getName());
            reportType(report.getType().name());
            List<Long> devIds = report.getDevices().stream().map(d -> d.getId()).collect(Collectors.toList());
            deviceIds(devIds);
            List<Long> gfIds = report.getGeoFences().stream().map(g->g.getId()).collect(Collectors.toList());
            geofenceIds(gfIds);
            fromDate(report.getFromDate());
            toDate(report.getToDate());
            includeMap(report.isIncludeMap());
            return disableFilter(report.isDisableFilter());
        }

        public EditReportDto build() {
            return new EditReportDto(name, reportType, deviceIds, geofenceIds, 
                    fromDate, toDate, includeMap, disableFilter);
        }
    }

    protected EditReportDto(final String name, final String reportType, 
            final List<Long> deviceIds, final List<Long> geofenceIds, 
            final Date fromDate, final Date toDate, final boolean includeMap, 
            final boolean disableFilter) {
        this.name = name;
        this.reportType = reportType;
        this.deviceIds = deviceIds;
        this.geofenceIds = geofenceIds;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.includeMap = includeMap;
        this.disableFilter = disableFilter;
    }
    
    
}
