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
package pl.datamatica.traccar.api.utils;

/**
 *
 * Extracted from org.traccar.web.server.model.GeoFenceCalculator
 */
public class GeoUtils {
    private static final double radKoef = Math.PI / 180;
    private static final double earthRadius = 6371.01; // Radius of the earth in km

    public static double getDistance(double lonX, double latX, double lonY, double latY) {
        double dLat = (latX - latY) * radKoef;
        double dLon = (lonX - lonY) * radKoef;
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(latX * radKoef) * Math.cos(latY * radKoef) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c; // Distance in km
    }
}
