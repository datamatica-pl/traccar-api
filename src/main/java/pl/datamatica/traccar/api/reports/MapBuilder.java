/*
 * Copyright 2017 Datamatica (dev@datamatica.pl)
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
package pl.datamatica.traccar.api.reports;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import pl.datamatica.traccar.api.metadata.model.DeviceIcon;
import pl.datamatica.traccar.model.DeviceEventType;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.GeoFence.LonLat;
import pl.datamatica.traccar.model.GeoFenceType;
import pl.datamatica.traccar.model.Position;

public class MapBuilder {    
    private final List<String> vectors = new ArrayList<>();
    private final List<String> markers = new ArrayList<>();
    private final String width, height;
    private List<String> tableIds = new ArrayList<>();
    private List<Integer> tableStartRows = new ArrayList<>();
    private List<Integer> featOffs = new ArrayList<>();
    
    public MapBuilder(String width, String height, Map<Long, IconData> icons, long defIconId) {
        this.width = width;
        this.height = height;
        MarkerStyle.icons = icons;
        MarkerStyle.defaultIconId = defIconId;
    }
    
    public MapBuilder polyline(List<Position> positions, String color, int width) {
        return polyline(PolylineEncoder.encode(positions), color, width);
    }
    
    public MapBuilder polyline(Coordinate[] points, String color, int width) {
        return polyline(PolylineEncoder.encode(points), color, width);
    }
    
    public MapBuilder polyline(String polyStr, String color, int width) {
        String id = "v"+vectors.size();
        StringBuilder sb = new StringBuilder();
        sb.append("var ").append(id).append(" = polyline('").append(polyStr).append("');\r\n");
        sb.append(id).append(".setStyle(new ol.style.Style({ stroke: new ol.style.Stroke({color: '").append(color).append("', width: ").append(width).append("})}));\r\n");
        
        vectors.add(sb.toString());
        return this;
    }
    
    public MapBuilder marker(Position position, MarkerStyle style) {
        String id = "m"+markers.size();
        StringBuilder sb = new StringBuilder();
        sb.append("var ").append(id).append(" = marker([").append(position.getLongitude())
                .append(", ").append(position.getLatitude()).append("], '');\r\n");
        sb.append(id).append(".setStyle(").append(style.compile()).append(");");
        
        markers.add(sb.toString());
        return this;
    }
    
    public MapBuilder geofence(GeoFence gf) {
        String id = "v"+vectors.size();
        List<LonLat> pt = gf.points();
        double r = gf.getRadius();
        String color = gf.getColor();
        String name = gf.getName();
        
        StringBuilder sb = new StringBuilder();
        sb.append("var ").append(id).append(" = ");
        switch(gf.getType()) {
            case CIRCLE:
                sb.append("circle([").append(pt.get(0).lon)
                .append(", ").append(pt.get(0).lat).append("], ").append(r)
                .append(", '").append(name).append("');\r\n");
                break;
            case POLYGON:
                sb.append("polygon([[");
                for(LonLat ll : pt)
                    sb.append("[").append(ll.lon).append(",").append(ll.lat).append("],");
                if(!pt.isEmpty())
                    sb.deleteCharAt(sb.length()-1);
                sb.append("]], '").append(name).append("');\r\n");
                break;
            case LINE:
                Coordinate[] coords = new Coordinate[gf.points().size()];
                for(int i=0;i<gf.points().size();++i) {
                    LonLat ll = gf.points().get(i);
                    coords[i] = new Coordinate(ll.lon, ll.lat);
                }
                sb.append("polyline('").append(PolylineEncoder.encode(coords)).append("');\r\n");
                break;
            default:
                return this;
        }
        
        if(GeoFenceType.LINE.equals(gf.getType())) {
            sb.append(id).append(".setStyle(lineStyle(").append(gf.getRadius())
                    .append(", ").append(gf.points().get(0).lat).append(", ")
                    .append(color).append("));\r\n");
        } else {
            sb.append(id).append(".setStyle(geoStyle('").append(name).append("', '")
                    .append(color).append("'));");
        }
        vectors.add(sb.toString());
        return this;
    }
    
    public MapBuilder bindWithTable(String id, int startRow) {
        return bindWithTable(id, startRow, -1);
    }
    
        public MapBuilder bindWithTable(String id, int startRow, int featOff) {
        tableIds.add(id);
        tableStartRows.add(startRow);
        if(featOff != -1)
            featOffs.add(featOff);
        return this;
    }
    
    public String create() {
        return create(false);
    }
    
    public String create(boolean showOnClick) {
        StringBuilder output = new StringBuilder();
        output.append("<div id=\"map\" style=\"width: ").append(width)
                .append("; height: ").append(height).append(";\"></div>\r\n");
        output.append("<script type=\"text/javascript\">\r\n");
        
        output.append("//helper functions\r\n");
        output.append(helperFunctions()).append("\r\n");
        output.append("//features\r\n");
        for(String v : vectors)
            output.append(v).append("\r\n");
        for(String m : markers)
            output.append(m).append("\r\n");
        
        output.append("var mfeat = [\r\n");
        for(int i=0;i<markers.size();++i) {
            output.append("m").append(i);
            if(i!= markers.size()-1)
                output.append(", ");
            else
                output.append("\r\n");
        }
        output.append("];\r\n");
        if(showOnClick)
            output.append("var msource = new ol.source.Vector({});\r\n");
        else
            output.append("var msource = new ol.source.Vector({features: mfeat});\r\n");
        output.append("var mlayer = new ol.layer.Vector({source: msource});");
        output.append("\r\n\r\n");
        
        output.append("var source = new ol.source.Vector({\r\n");
        output.append("  features: [\r\n");
        for(int i=0;i<vectors.size();++i) {
            output.append("v").append(i);
            if(i != vectors.size()-1)
                output.append(", ");
            else
                output.append("\r\n");
        }
        output.append("  ]\r\n");
        output.append("});\r\n\r\n");
        
        
        output.append("//the map\r\n");
        output.append("var map = new ol.Map({\r\n");
        output.append("  target: 'map',\r\n");
        output.append("  layers: [\r\n");
        output.append("    new ol.layer.Tile({source: new ol.source.OSM()}),\r\n");
        output.append("    new ol.layer.Vector({\r\n");
        output.append("      source: source\r\n");
        output.append("    }),\r\n")
              .append("    mlayer\r\n");
        output.append("  ],\r\n");
        output.append("  view: new ol.View({\r\n");
        output.append("    zoom: 12\r\n");
        output.append("  })\r\n");
        output.append("});\r\n"); 
        output.append("map.getView().fit(boundingBox(source.getFeatures().concat(mfeat)), map.getSize());\r\n");
        
        for(int i=0;i<tableIds.size();++i) {
            output.append("bind(map, '").append(tableIds.get(i)).append("', ")
                    .append(tableStartRows.get(i)).append(", ");
            if(showOnClick)
                output.append(featOffs.get(i));
            else
                output.append("-1");
            output.append(");\r\n");
        }
        output.append("</script>");
        return output.toString();
    }
    
    private String helperFunctions() {
        return "function polyline(polyString) {\r\n"
                + "  var routeGeom = new ol.format.Polyline().readGeometry(polyString, "
                + "    {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});\r\n"
                + "  return new ol.Feature({ geometry: routeGeom, name: 'Route'});\r\n"  
                + "}\r\n"
                + "function marker(coords, name) {\r\n"
                + "  var geom = new ol.geom.Point(ol.proj.transform(coords, 'EPSG:4326', 'EPSG:3857'));\r\n"
                + "  return new ol.Feature({ geometry: geom, name: name});\r\n"
                + "}\r\n"
                + "function polygon(coords, name) {\r\n"
                + "  var geom = new ol.geom.Polygon(coords).transform('EPSG:4326', 'EPSG:3857');\r\n"
                + "  return new ol.Feature({ geometry: geom, name: name });\r\n"
                + "}"
                +"function circle(coords, radius, name) {\r\n"
                + "  var geom = new ol.geom.Circle(ol.proj.transform(coords, 'EPSG:4326', 'EPSG:3857'), radius);\r\n"
                + "  return new ol.Feature({ geometry: geom, name: name});\r\n"
                + "}\r\n"
                + "function fillColor(hex) {\r\n"
                + "  var tmp = parseInt(hex, 16);\r\n"
                + "  return 'rgba('+((tmp>>16)&255)+', '+((tmp>>8)&255)+', '"
                + "      +(tmp&255)+', 0.5)';\r\n"
                + "}\r\n"
                + "function boundingBox(markers) {\r\n"
                + "  var p1=ol.proj.transform([-180, -90], 'EPSG:4326', 'EPSG:3857');\r\n"
                + "  var p2=ol.proj.transform([180, 90], 'EPSG:4326', 'EPSG:3857');\r\n"
                + "  for(var i=0;i<markers.length;++i) {\r\n"
                + "    var ext = markers[i].getGeometry().getExtent();\r\n"
                + "    p1[0] = Math.max(p1[0], ext[2]);\r\n"
                + "    p1[1] = Math.max(p1[1], ext[3]);\r\n"
                + "    p2[0] = Math.min(p2[0], ext[0]);\r\n"
                + "    p2[1] = Math.min(p2[1], ext[1]);\r\n"
                + "  }\r\n"
                + "  var lonDiff = p2[0]-p1[0], latDiff = p2[1]-p1[1];\r\n"
                + "  var delta = (1000 - lonDiff)/2;\r\n"
                + "  if(delta > 0) {\r\n"
                + "    p2[0] -= delta;\r\n"
                + "    p1[0] += delta;\r\n"
                + "  }\r\n"
                + "  delta = (1000-latDiff)/2;\r\n"
                + "  if(delta > 0) {\r\n"
                + "    p2[1] -= delta;\r\n"
                + "    p1[1] += delta;\r\n"
                + "  }\r\n"
                + "  return [p2[0], p2[1], p1[0], p1[1]];\r\n"
                + "}\r\n"
                + "function geoStyle(name, color) {\r\n"
                + "  return new ol.style.Style({\r\n"
                + "    fill: new ol.style.Fill({ color: fillColor(color) }),\r\n"
                + "    stroke: new ol.style.Stroke({ color: \"#\"+color }),\r\n"
                + "    text: new ol.style.Text({\r\n"
                + "      text: name,\r\n"
                + "      font: 'bold 14px Arial, sans-serif'\r\n"
                + "    })\r\n"
                + "  });\r\n"
                + "}\r\n"
                + "function bind(map, tableId, startRow, featOff) {\r\n"
                + "  var table = document.getElementById(tableId);\r\n"
                + "  var rows = table.getElementsByTagName('tr');\r\n"
                + "  for(i=startRow;i<rows.length;++i) {\r\n"
                + "    var row = rows[i];\r\n"
                + "    row.onclick = function(i) {\r\n"
                + "      return function() {\r\n"
                + "        var r = rows[i];\r\n"
                + "        console.log(i);\r\n"
                + "        for(j=startRow;j<rows.length;++j)\r\n"
                + "          rows[j].classList.remove('active');\r\n"
                + "        r.classList.add('active');\r\n"
                + "        var cells = r.getElementsByTagName('td');\r\n"
                + "        var c = cells[cells.length-1];\r\n"
                + "        var extent = ol.proj.transformExtent(JSON.parse(c.innerHTML), 'EPSG:4326', 'EPSG:3857');\r\n"
                + "        map.getView().fit(extent, map.getSize());\r\n"
                + "        if(featOff != -1) {\r\n"
                + "          msource.clear();\r\n"
                + "          msource.addFeature(mfeat[i-startRow+featOff]);\r\n"
                + "        }\r\n"
                + "      };\r\n"
                + "    }(i);\r\n"
                + "  }\r\n"
                + "}\r\n"
                + "function lineStyle(widthInMeters, lat, color) {\r\n"
                + "  var w = widthInMeters / Math.cos(lat*Math.PI/180);\r\n"
                + "  return function(resolution) {\r\n"
                + "    console.log(w/resolution);\r\n"
                + "    return [new ol.style.Style({\r\n"
                + "      stroke: new ol.style.Stroke({\r\n"
                + "        width: w / resolution,\r\n"
                + "        color: fillColor(color)\r\n"
                + "      })\r\n"
                + "    })];\r\n"
                + "  };\r\n"
                + "}\r\n";
    }
    
    
    
    public static class MarkerStyle {
        private String image;
        private String text;
        private static Map<Long, IconData> icons;
        private static long defaultIconId;
        
        public String compile() {
            StringBuilder sb = new StringBuilder("new ol.style.Style({\r\n");
            if(image != null)
                sb.append("  image: ").append(image).append(",\r\n");
            if(text != null) 
                sb.append("  text: ").append(text).append(",\r\n");
            sb.append("})");
            return sb.toString();
        }
        
        public static MarkerStyle arrow(Double rotation) {
            MarkerStyle style = new MarkerStyle();
            String icon = "new ol.style.Icon({"
                    + "src: '/MapMarker?color=000000', anchor: [0.5, 0.5],"
                    + "anchorXUnits: 'fraction', anchorYUnits: 'fraction'";
            if(rotation != null)
                icon += ", rotation: "+ rotation/180*Math.PI;
            icon += "})";
            style.image = icon;
            return style;
        }
        
        public static MarkerStyle routeStart() {
            MarkerStyle style = new MarkerStyle();
            style.image = "new ol.style.Icon({ "
                + "anchor: [0.5, 25], anchorXUnits: 'fraction', anchorYUnits: 'pixels', "
                + "opacity: 0.75, src: 'https://cdnjs.cloudflare.com/ajax/libs/openlayers/2.13.1/img/marker.png' })";
            return style;
        }
        
        public static MarkerStyle routeEnd() {
            MarkerStyle style = new MarkerStyle();
            style.image = "new ol.style.Icon({ "
                + "anchor: [0.5, 25], anchorXUnits: 'fraction', anchorYUnits: 'pixels', "
                + "opacity: 0.75, src: 'https://cdnjs.cloudflare.com/ajax/libs/openlayers/2.13.1/img/marker-blue.png' })";
            return style;
        }
        
        public static MarkerStyle deviceMarker(Position position) {
            MarkerStyle style = new MarkerStyle();
            IconData data = icons.get(position.getDevice().getIconId());
            if(data == null)
                data = icons.get(defaultIconId);
            style.image = "new ol.style.Icon({src: '"+data.getUrl()+"', "
                    + "anchor: "+data.getAnchor()+", scale: "+data.getScale()+
                    (data.canRotate && position.getCourse() != null ? 
                    ", rotation: "+ position.getCourse()*Math.PI/180 : "")+"})";
            return style;
        }
        
        public static MarkerStyle event(DeviceEventType type, String label) {
            MarkerStyle style = new MarkerStyle();
            style.image = String.format("new ol.style.Icon({src: '%s', anchor: [0.5, 1]})",
                    src(type));
            style.text = String.format("new ol.style.Text({ text: '%s', offsetY: -60, "
                    + "font: 'bold 11px Arial, sans-serif'})", label);
            return style;
        }
        
        private static String src(DeviceEventType type) {
            return "/img/event_"+type.name()+".png";
        }
    }
    
    public static class MapBoundsBuilder {
        private double minLon=180, minLat=180, maxLon=-180, maxLat=-180;
        
        public MapBoundsBuilder addPosition(Position p) {
            minLat = Math.min(minLat, p.getLatitude());
            minLon = Math.min(minLon, p.getLongitude());
            maxLat = Math.max(maxLat, p.getLatitude());
            maxLon = Math.max(maxLon, p.getLongitude());
            return this;
        }
        
        public String create(double minSize) { 
            if(maxLat - minLat < minSize) {
                double dLat = minSize-maxLat+minLat;
                minLat -= dLat/2;
                maxLat += dLat/2;
            }
            if(maxLon - minLon < minSize) {
                double dLon = minSize-maxLon+minLon;
                minLon -= dLon;
                maxLon += dLon;
            }
            
            return String.format(Locale.US, "[%f,%f,%f,%f]", 
                    minLon, minLat, maxLon, maxLat);
        }
    }
    
    public static class IconData {
        private boolean canRotate;
        private String url;
        
        public IconData(DeviceIcon di) {
            this.url = di.getIconUrl();
            this.canRotate = di.isWithoutFrame();
            
            if(!di.isWithoutFrame()) {
                url = url.replace("/images/", "/markers/");
            }
        }
        
        public String getUrl() {
            return url;
        }
        
        public String getAnchor() {
            return canRotate ? "[0.5, 0.5]" : "[0.5, 1]";
        }
        
        public float getScale() {
            return canRotate ? 0.25f : 1;
        }
    }
}
