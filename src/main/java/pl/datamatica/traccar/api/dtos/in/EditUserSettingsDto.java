/*
 *  Copyright (C) 2017  Datamatica (dev@datamatica.pl)
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
import java.util.List;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;

/**
 *
 * @author admin
 */
public class EditUserSettingsDto {
    public static List<ErrorDto> validate(EditUserSettingsDto dto) {
        if(dto == null)
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_DATA_NOT_PROVIDED));
        List<ErrorDto> errors = new ArrayList<>();
        if(dto.centerLatitude == null)
            errors.add(new ErrorDto(MessageKeys.ERR_USERSETTINGS_CENTER_LATITUDE_NOT_PROVIDED));
        if(dto.centerLongitude == null)
            errors.add(new ErrorDto(MessageKeys.ERR_USERSETTINGS_CENTER_LONGITUDE_NOT_PROVIDED));
        if(dto.hideDuplicates == null)
            errors.add(new ErrorDto(MessageKeys.ERR_USERSETTINGS_HIDE_DUPLICATES_NOT_PROVIDED));
        if(dto.hideInvalidLocations == null)
            errors.add(new ErrorDto(MessageKeys.ERR_USERSETTINGS_HIDE_INVALID_LOCATIONS_NOT_PROVIDED));
        if(dto.hideZeroCoordinates == null)
            errors.add(new ErrorDto(MessageKeys.ERR_USERSETTINGS_HIDE_ZERO_COORDINATES_NOT_PROVIDED));
        if(dto.mapType == null || dto.mapType.isEmpty())
            errors.add(new ErrorDto(MessageKeys.ERR_USERSETTINGS_MAP_TYPE_NOT_PROVIDED));
        if(dto.maximizeOverviewMap == null)
            errors.add(new ErrorDto(MessageKeys.ERR_USERSETTINGS_MAXIMIZE_OVERVIEW_MAP_NOT_PROVIDED));
        if(dto.overlays == null)
            errors.add(new ErrorDto(MessageKeys.ERR_USERSETTINGS_OVERLAYS_NOT_PROVIDED));
        if(dto.timePrintInterval == null)
            errors.add(new ErrorDto(MessageKeys.ERR_TIME_PRINT_INTERVAL_NOT_PROVIDED));
        if(dto.timeZoneId == null)
            errors.add(new ErrorDto(MessageKeys.ERR_TIME_ZONE_ID_NOT_PROVIDED));
        if(dto.zoomLevel == null)
            errors.add(new ErrorDto(MessageKeys.ERR_ZOOM_LEVEL_NOT_PROVIDED));
        return errors;
    }
    
    private final String archiveMarkerType;
    private final Double centerLatitude;
    private final Double centerLongitude;
    private final Boolean hideDuplicates;
    private final Boolean hideInvalidLocations;
    private final Boolean hideZeroCoordinates;
    private final String mapType;
    private final Boolean maximizeOverviewMap;
    private final Double minDistance;
    private final String overlays;
    private final Double speedForFilter;
    private final String speedModifier;
    private final String speedUnit;
    private final Short timePrintInterval;
    private final String timeZoneId;
    private final Short traceInterval;
    private final Integer zoomLevel;
    private final Short followedDeviceZoomLevel;
    
    public EditUserSettingsDto(String archiveMarkerType, double centerLatitude, double centerLongitude, 
            boolean hideDuplicates, boolean hideInvalidLocations, boolean hideZeroCoordinates, 
            String mapType, boolean maximizeOverviewMap, Double minDistance, String overlays, Double speedForFilter, 
            String speedModifier, String speedUnit, short timePrintInterval, String timeZoneId, Short traceInterval, int zoomLevel, Short followedDeviceZoomLevel) {
        this.archiveMarkerType = archiveMarkerType;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.hideDuplicates = hideDuplicates;
        this.hideInvalidLocations = hideInvalidLocations;
        this.hideZeroCoordinates = hideZeroCoordinates;
        this.mapType = mapType;
        this.maximizeOverviewMap = maximizeOverviewMap;
        this.minDistance = minDistance;
        this.overlays = overlays;
        this.speedForFilter = speedForFilter;
        this.speedModifier = speedModifier;
        this.speedUnit = speedUnit;
        this.timePrintInterval = timePrintInterval;
        this.timeZoneId = timeZoneId;
        this.traceInterval = traceInterval;
        this.zoomLevel = zoomLevel;
        this.followedDeviceZoomLevel = followedDeviceZoomLevel;
    }
    
    public String getArchiveMarkerType() {
        return archiveMarkerType;
    }

    public double getCenterLatitude() {
        return centerLatitude;
    }

    public double getCenterLongitude() {
        return centerLongitude;
    }

    public boolean isHideDuplicates() {
        return hideDuplicates;
    }

    public boolean isHideInvalidLocations() {
        return hideInvalidLocations;
    }

    public boolean isHideZeroCoordinates() {
        return hideZeroCoordinates;
    }

    public String getMapType() {
        return mapType;
    }

    public boolean isMaximizeOverviewMap() {
        return maximizeOverviewMap;
    }

    public Double getMinDistance() {
        return minDistance;
    }

    public String getOverlays() {
        return overlays;
    }

    public Double getSpeedForFilter() {
        return speedForFilter;
    }

    public String getSpeedModifier() {
        return speedModifier;
    }

    public String getSpeedUnit() {
        return speedUnit;
    }

    public short getTimePrintInterval() {
        return timePrintInterval;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public Short getTraceInterval() {
        return traceInterval;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public Short getFollowedDeviceZoomLevel() {
        return followedDeviceZoomLevel;
    }

    public static class Builder {

        private String archiveMarkerType;
        private double centerLatitude;
        private double centerLongitude;
        private boolean hideDuplicates;
        private boolean hideInvalidLocations;
        private boolean hideZeroCoordinates;
        private String mapType;
        private boolean maximizeOverviewMap;
        private Double minDistance;
        private String overlays;
        private Double speedForFilter;
        private String speedModifier;
        private String speedUnit;
        private short timePrintInterval;
        private String timeZoneId;
        private Short traceInterval;
        private int zoomLevel;
        private Short followedDeviceZoomLevel;

        public Builder() {
        }

        public Builder archiveMarkerType(final String value) {
            this.archiveMarkerType = value;
            return this;
        }

        public Builder centerLatitude(final double value) {
            this.centerLatitude = value;
            return this;
        }

        public Builder centerLongitude(final double value) {
            this.centerLongitude = value;
            return this;
        }

        public Builder hideDuplicates(final boolean value) {
            this.hideDuplicates = value;
            return this;
        }

        public Builder hideInvalidLocations(final boolean value) {
            this.hideInvalidLocations = value;
            return this;
        }

        public Builder hideZeroCoordinates(final boolean value) {
            this.hideZeroCoordinates = value;
            return this;
        }

        public Builder mapType(final String value) {
            this.mapType = value;
            return this;
        }

        public Builder maximizeOverviewMap(final boolean value) {
            this.maximizeOverviewMap = value;
            return this;
        }

        public Builder minDistance(final Double value) {
            this.minDistance = value;
            return this;
        }

        public Builder overlays(final String value) {
            this.overlays = value;
            return this;
        }

        public Builder speedForFilter(final Double value) {
            this.speedForFilter = value;
            return this;
        }

        public Builder speedModifier(final String value) {
            this.speedModifier = value;
            return this;
        }

        public Builder speedUnit(final String value) {
            this.speedUnit = value;
            return this;
        }

        public Builder timePrintInterval(final short value) {
            this.timePrintInterval = value;
            return this;
        }

        public Builder timeZoneId(final String value) {
            this.timeZoneId = value;
            return this;
        }

        public Builder traceInterval(final Short value) {
            this.traceInterval = value;
            return this;
        }

        public Builder zoomLevel(final int value) {
            this.zoomLevel = value;
            return this;
        }

        public Builder followedDeviceZoomLevel(final Short value) {
            this.followedDeviceZoomLevel = value;
            return this;
        }

        public EditUserSettingsDto build() {
            return new pl.datamatica.traccar.api.dtos.in.EditUserSettingsDto(archiveMarkerType, centerLatitude, centerLongitude, hideDuplicates, hideInvalidLocations, hideZeroCoordinates, mapType, maximizeOverviewMap, minDistance, overlays, speedForFilter, speedModifier, speedUnit, timePrintInterval, timeZoneId, traceInterval, zoomLevel, followedDeviceZoomLevel);
        }
    }    
}
