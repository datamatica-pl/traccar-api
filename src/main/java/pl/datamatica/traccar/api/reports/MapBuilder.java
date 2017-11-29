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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import pl.datamatica.traccar.model.DeviceEventType;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.GeoFence.LonLat;
import pl.datamatica.traccar.model.Position;

public class MapBuilder {    
    private final List<String> vectors = new ArrayList<>();
    private final String width, height;
    private List<String> tableIds = new ArrayList<>();
    private List<Integer> tableStartRows = new ArrayList<>();
    
    public MapBuilder(String width, String height, Map<Long, String> icons) {
        this.width = width;
        this.height = height;
        MarkerStyle.icons = icons;
    }
    
    public MapBuilder polyline(List<Position> positions, String color, int width) {
        String id = "v"+vectors.size();
        StringBuilder sb = new StringBuilder();
        sb.append("var ").append(id).append(" = polyline('").append(PolylineEncoder.encode(positions)).append("');\r\n");
        sb.append(id).append(".setStyle(new ol.style.Style({ stroke: new ol.style.Stroke({color: '").append(color).append("', width: ").append(width).append("})}));\r\n");
        
        vectors.add(sb.toString());
        return this;
    }
    
    public MapBuilder marker(Position position, MarkerStyle style) {
        String id = "v"+vectors.size();
        StringBuilder sb = new StringBuilder();
        sb.append("var ").append(id).append(" = marker([").append(position.getLongitude())
                .append(", ").append(position.getLatitude()).append("], '');\r\n");
        sb.append(id).append(".setStyle(").append(style.compile()).append(");");
        
        vectors.add(sb.toString());
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
            default:
                return this;
        }
        
        sb.append(id).append(".setStyle(geoStyle('").append(name).append("', '")
                .append(color).append("'));");
        vectors.add(sb.toString());
        return this;
    }
    
    public MapBuilder bindWithTable(String id, int startRow) {
        tableIds.add(id);
        tableStartRows.add(startRow);
        return this;
    }
    
    public String create() {
        StringBuilder output = new StringBuilder();
        output.append("<div id=\"map\" style=\"width: ").append(width)
                .append("; height: ").append(height).append(";\"></div>\r\n");
        output.append("<script type=\"text/javascript\">\r\n");
        
        output.append("//helper functions\r\n");
        output.append(helperFunctions()).append("\r\n");
        output.append("//features\r\n");
        for(String v : vectors)
            output.append(v).append("\r\n");
        
        output.append("var source = new ol.source.Vector({\r\n");
        output.append("  features: [\r\n");
        for(int i=0;i<vectors.size();++i) {
            output.append("          v").append(i);
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
        output.append("    })\r\n");
        output.append("  ],\r\n");
        output.append("  view: new ol.View({\r\n");
        output.append("    zoom: 12\r\n");
        output.append("  })\r\n");
        output.append("});\r\n");
        if(vectors.size() >= 2)
            output.append("map.getView().fit(source.getExtent(), map.getSize());\r\n");
        else if(vectors.size() == 1)
            output.append("map.getView().setCenter(v0.getGeometry().getCoordinates());\r\n");
        
        for(int i=0;i<tableIds.size();++i) {
            output.append("bind(map, '").append(tableIds.get(i)).append("', ")
                    .append(tableStartRows.get(i)).append(");\r\n");
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
                + "function bind(map, tableId, startRow) {\r\n"
                + "  var table = document.getElementById(tableId);\r\n"
                + "  var rows = table.getElementsByTagName('tr');\r\n"
                + "  for(i=startRow;i<rows.length;++i) {\r\n"
                + "    var row = rows[i];\r\n"
                + "    var cells = row.getElementsByTagName('td');\r\n"
                + "    var extCell = cells[cells.length-1];\r\n"
                + "    row.onclick = function(c) {\r\n"
                + "      return function() {\r\n"
                + "        var extent = ol.proj.transformExtent(JSON.parse(c.innerHTML), 'EPSG:4326', 'EPSG:3857');"
                + "        map.getView().fit(extent, map.getSize());\r\n"
                + "      };\r\n"
                + "    }(extCell);\r\n"
                + "  }\r\n"
                + "}\r\n";
    }
    
    
    
    public static class MarkerStyle {
        private String image;
        private String text;
        private static Map<Long, String> icons;
        
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
            String url = icons.get(position.getDevice().getIconId());
            style.image = "new ol.style.Icon({src: '/"+url+"', anchor: [0.5, 1]})";
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
}
