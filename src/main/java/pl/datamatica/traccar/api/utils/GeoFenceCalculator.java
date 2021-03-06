/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.datamatica.traccar.api.utils;

import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Position;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoFenceCalculator {
    private static final double radKoef = Math.PI / 180;
    private static final double earthRadius = 6371.01; // Radius of the earth in km

    private static class GeoFenceData {
        final List<GeoFence.LonLat> points;
        final Shape shape;

        GeoFenceData(List<GeoFence.LonLat> points, Shape shape) {
            this.points = points;
            this.shape = shape;
        }
    }

    private final Map<GeoFence, GeoFenceData> geoFences;

    public GeoFenceCalculator(Collection<GeoFence> geoFences) {
        this.geoFences = new HashMap<>(geoFences.size());
        for (GeoFence geoFence : geoFences) {
            List<GeoFence.LonLat> points = geoFence.points();
            Shape shape = null;
            switch (geoFence.getType()) {
                case POLYGON:
                    Path2D polygon = new Path2D.Double();
                    for (GeoFence.LonLat point : geoFence.points()) {
                        if (polygon.getCurrentPoint() == null) {
                            polygon.moveTo(point.lon, point.lat);
                        } else {
                            polygon.lineTo(point.lon, point.lat);
                        }
                    }
                    polygon.closePath();
                    shape = polygon;
                    break;
            }
            this.geoFences.put(geoFence, new GeoFenceData(points, shape));
        }
    }

    public boolean contains(GeoFence geoFence, Position position) {
        return contains(geoFence, position, false);
    }
    
    public boolean contains(GeoFence geoFence, Position position, boolean skipCheck) {
        if (!skipCheck && (position.getDevice() == null 
                || !geoFence.getDevices().contains(position.getDevice()))) {
            return false;
        }

        GeoFenceData data = geoFences.get(geoFence);
        switch (geoFence.getType()) {
            case POLYGON:
                return data.shape.contains(position.getLongitude(), position.getLatitude());
            case CIRCLE:
                GeoFence.LonLat center = data.points.get(0);
                return GeoUtils.getDistance(position.getLongitude(), position.getLatitude(), center.lon, center.lat) <= geoFence.getRadius() / 1000;
            case LINE:
                GeoFence.LonLat prevPoint = null;
                for (GeoFence.LonLat point : data.points) {
                    if (prevPoint != null) {
                        // from http://stackoverflow.com/questions/1459368/snap-point-to-a-line
                        double apx = position.getLongitude() - prevPoint.lon;
                        double apy = position.getLatitude() - prevPoint.lat;
                        double abx = point.lon - prevPoint.lon;
                        double aby = point.lat - prevPoint.lat;

                        double ab2 = abx * abx + aby * aby;
                        double ap_ab = apx * abx + apy * aby;
                        double t = ap_ab / ab2;
                        if (t < 0) {
                            t = 0;
                        } else if (t > 1) {
                            t = 1;
                        }

                        double destLon = prevPoint.lon + abx * t;
                        double destLat = prevPoint.lat + aby * t;

                        if (GeoUtils.getDistance(destLon, destLat, position.getLongitude(), position.getLatitude()) <= geoFence.getRadius() / 2000) {
                            return true;
                        }
                    }
                    prevPoint = point;
                }
                break;
        }

        return false;
    }
}
