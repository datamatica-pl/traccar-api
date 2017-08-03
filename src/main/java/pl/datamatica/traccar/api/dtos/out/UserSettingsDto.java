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
package pl.datamatica.traccar.api.dtos.out;

import pl.datamatica.traccar.model.UserSettings;

/**
 *
 * @author ŁŁ
 */
public class UserSettingsDto {
    private final long id;
    private final String archiveMarkerType;
    private final double centerLatitude;
    private final double centerLongitude;
    private final boolean hideDuplicates;
    private final boolean hideInvalidLocations;
    private final boolean hideZeroCoordinates;
    private final String mapType;
    private final boolean maximizeOverviewMap;
    private final Double minDistance;
    private final String overlays;
    private final Double speedForFilter;
    private final String speedModifier;
    private final String speedUnit;
    private final short timePrintInterval;
    private final String timeZoneId;
    private final Short traceInterval;
    private final int zoomLevel;
    private final Short followedDeviceZoomLevel;

    public UserSettingsDto(long id, String archiveMarkerType, double centerLatitude, double centerLongitude, 
            boolean hideDuplicates, boolean hideInvalidLocations, boolean hideZeroCoordinates, 
            String mapType, boolean maximizeOverviewMap, Double minDistance, String overlays, 
            Double speedForFilter, String speedModifier, String speedUnit, short timePrintInterval, 
            String timeZoneId, Short traceInterval, int zoomLevel, Short followedDeviceZoomLevel) {
        this.id = id;
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

    public static class Builder {
        
        private long id;
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

        public Builder id(final long value) {
            this.id = value;
            return this;
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

        public Builder zoomLevel(final byte value) {
            this.zoomLevel = value;
            return this;
        }

        public Builder followedDeviceZoomLevel(final Short value) {
            this.followedDeviceZoomLevel = value;
            return this;
        }
        
        public Builder userSettings(final UserSettings us) {
            if(us.getArchiveMarkerType() != null)
                this.archiveMarkerType = us.getArchiveMarkerType().name();
            this.centerLatitude = us.getCenterLatitude();
            this.centerLongitude = us.getCenterLongitude();
            this.followedDeviceZoomLevel = us.getFollowedDeviceZoomLevel();
            this.hideDuplicates = us.isHideDuplicates();
            this.hideInvalidLocations = us.isHideInvalidLocations();
            this.hideZeroCoordinates = us.isHideZeroCoordinates();
            this.id = us.getId();
            this.mapType = us.getMapType().name();
            this.maximizeOverviewMap = us.isMaximizeOverviewMap();
            this.minDistance = us.getMinDistance();
            this.overlays = us.getOverlays();
            this.speedForFilter = us.getSpeedForFilter();
            this.speedModifier = us.getSpeedModifier();
            this.speedUnit = us.getSpeedUnit().name();
            this.timePrintInterval = us.getTimePrintInterval();
            this.timeZoneId = us.getTimeZoneId();
            this.traceInterval = us.getTraceInterval();
            this.zoomLevel = us.getZoomLevel();
            return this;
        }

        public UserSettingsDto build() {
            return new UserSettingsDto(id, archiveMarkerType, centerLatitude, centerLongitude, 
                    hideDuplicates, hideInvalidLocations, hideZeroCoordinates, mapType, 
                    maximizeOverviewMap, minDistance, overlays, speedForFilter, 
                    speedModifier, speedUnit, timePrintInterval, timeZoneId, traceInterval, 
                    zoomLevel, followedDeviceZoomLevel);
        }
    }

    public long getId() {
        return id;
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
    
    
}
