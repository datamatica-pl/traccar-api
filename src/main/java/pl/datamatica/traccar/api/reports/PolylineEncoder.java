package pl.datamatica.traccar.api.reports;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.ArrayList;
import pl.datamatica.traccar.model.Position;

import java.util.List;
import pl.datamatica.traccar.model.GeoFence.LonLat;

/**
 * Methods to encode and decode a polyline with Google polyline encoding/decoding scheme.
 * See https://developers.google.com/maps/documentation/utilities/polylinealgorithm
 */
public class PolylineEncoder {

    private PolylineEncoder() {
    }

    private static StringBuilder encodeSignedNumber(int num) {
        int sgn_num = num << 1;
        if (num < 0) {
            sgn_num = ~(sgn_num);
        }
        return encodeNumber(sgn_num);
    }

    private static StringBuilder encodeNumber(int num) {
        StringBuilder encodeString = new StringBuilder();
        while (num >= 0x20) {
            int nextValue = (0x20 | (num & 0x1f)) + 63;
            if (nextValue == 92) {
                encodeString.append((char)(nextValue));
            }
            encodeString.append((char)(nextValue));
            num >>= 5;
        }

        num += 63;
        if (num == 92) {
            encodeString.append((char)(num));
        }

        encodeString.append((char)(num));

        return encodeString;
    }

    /**
     * Encode a polyline with Google polyline encoding method
     * @param polyline the polyline
     * @return the encoded polyline, as a String
     */
    public static String encode(List<Position> polyline) {
        StringBuilder encodedPoints = new StringBuilder();
        int prev_lat = 0, prev_lng = 0;
        for (Position trackpoint : polyline) {
            int lat = (int) Math.round(trackpoint.getLatitude() * 1e5);
            int lng = (int) Math.round(trackpoint.getLongitude() * 1e5);
            encodedPoints.append(encodeSignedNumber(lat - prev_lat));
            encodedPoints.append(encodeSignedNumber(lng - prev_lng));
            prev_lat = lat;
            prev_lng = lng;
        }
        return encodedPoints.toString();
    }
    
    public static String encode(Coordinate[] points) {
        StringBuilder encodedPoints = new StringBuilder();
        int prev_lat = 0, prev_lng = 0;
        for (Coordinate trackpoint : points) {
            int lat = (int) Math.round(trackpoint.y * 1e5);
            int lng = (int) Math.round(trackpoint.x * 1e5);
            encodedPoints.append(encodeSignedNumber(lat - prev_lat));
            encodedPoints.append(encodeSignedNumber(lng - prev_lng));
            prev_lat = lat;
            prev_lng = lng;
        }
        return encodedPoints.toString();
    }
    
    public static List<LonLat> decode(String encoded) {
        PolylineParser parser = new PolylineParser(encoded);
        List<LonLat> coords = new ArrayList<>();
        double lat = 0, lon = 0;
        while(parser.hasNext()) {
            int dLat = parser.parseNext();
            lat += dLat / 1.e5;
            int dLon = 0;
            if(parser.hasNext())
                dLon = parser.parseNext();
            lon += dLon / 1.e5;
            coords.add(new LonLat(lon, lat));
        }
        return coords;
    }
    
    private static class PolylineParser {
        private int i = -1;
        private final String encoded;
        
        public PolylineParser(String encoded) {
            this.encoded = encoded;
        }
        
        public int parseNext() {
            ++i;
            int res = 0, s = 0;
            while(((encoded.charAt(i) - 63) & 0x20) != 0) {
                res |= ((encoded.charAt(i) - 63) & 0x1F) << s;
                s+=5;
                ++i;
            }
            if((res & 1) != 0) {
                res = ~res;
            }
            res >>= 1;
            return res;
        }
        
        public boolean hasNext() {
            return encoded.length() - 1 > i;
        }
    }
}
