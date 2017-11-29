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
package pl.datamatica.traccar.api.reports;

import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Device;

import java.io.IOException;
import java.util.*;
import pl.datamatica.traccar.api.dtos.out.ReportDto;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.utils.GeoFenceCalculator;
import pl.datamatica.traccar.model.DeviceEvent;
import pl.datamatica.traccar.model.DeviceEventType;

public class ReportGFIO extends ReportGenerator {
    @Override
    void generateImpl(ReportDto report) throws IOException, ProviderException {
        h2(report.getName());

        for (Device device : getDevices(report)) {
            Date from = getFromDate(report, device);
            List<GeoFence> geoFences = getGeoFences(report, device);
            List<Position> positions = getPositions(device, 
                    from, report.getToDate(), report.isDisableFilter());

            panelStart();

            // heading
            panelHeadingStart();
            text(device.getName());
            panelHeadingEnd();

            // body
            panelBodyStart();
            // period
            paragraphStart();
            bold(message("report_time_period") + ": ");
            text(formatDate(from) + " - " + formatDate(report.getToDate()));
            paragraphEnd();
            // device details
            deviceDetails(device);
            // data table
            if (!positions.isEmpty() && !geoFences.isEmpty()) {
                List<DeviceEvent> events = calculate(geoFences, positions);
                if(report.isIncludeMap() && !events.isEmpty()) {
                    html("<div class=\"col-md-6\">");
                }
                drawTable(events);
                if(report.isIncludeMap() && !events.isEmpty()) {
                    html("</div>");
                    html("<div class=\"col-md-6\">");
                    drawMap(events, geoFences);
                    html("</div>");
                }
            }

            panelBodyEnd();

            panelEnd();
        }
    }

    List<DeviceEvent> calculate(List<GeoFence> geoFences, List<Position> positions) {
        // calculate
        GeoFenceCalculator calculator = new GeoFenceCalculator(geoFences);
        Set<GeoFence> currentData = new HashSet<>(geoFences.size());
        List<DeviceEvent> result = new ArrayList<>();

        for (Position position : positions) {
            for (GeoFence geoFence : geoFences) {
                if (calculator.contains(geoFence, position)) {
                    if (!currentData.contains(geoFence)) {
                        DeviceEvent ev = new DeviceEvent(position.getTime(), 
                                position.getDevice(), position, geoFence, null);
                        ev.setType(DeviceEventType.GEO_FENCE_ENTER);
                        result.add(ev);
                        currentData.add(geoFence);
                    }
                } else {
                    boolean geoFenceData = currentData.remove(geoFence);
                    if (geoFenceData) {
                        DeviceEvent ev = new DeviceEvent(position.getTime(), 
                                position.getDevice(), position, geoFence, null);
                        ev.setType(DeviceEventType.GEO_FENCE_EXIT);
                        result.add(ev);
                    }
                }
            }
        }
        return result;
    }

    void drawTable(List<DeviceEvent> datas) {

        // draw
        tableStart("table", hover().condensed().height(DEFAULT_TABLE_HEIGHT));

        // header
        tableHeadStart();
        tableRowStart();

        String[] GFIO_report_headers = new String[]{"report_header_geofence_name", 
            "report_time", "report_event"};

        for (String report_header : GFIO_report_headers) {
            tableHeadCellStart();
            text(message(report_header));
            tableHeadCellEnd();
        }

        tableRowEnd();
        tableHeadEnd();

        // body
        tableBodyStart();

        for (DeviceEvent data : datas) {
            tableRowStart();
            tableCell(data.getGeoFence().getName());
            tableCell(formatDate(data.getTime()));
            tableCell(message("report_event_" + data.getType().name().toLowerCase()));
            extentCell(data.getPosition(), data.getPosition());
            tableRowEnd();
        }

        tableBodyEnd();

        tableEnd();
    }
    
    void drawMap(List<DeviceEvent> events, List<GeoFence> gfs) {
        MapBuilder builder = getMapBuilder();
        for(DeviceEvent ev : events) {
            builder.marker(ev.getPosition(), 
                    MapBuilder.MarkerStyle.event(ev.getType(), ""));
        }
        for(GeoFence gf : gfs)
            builder.geofence(gf);
        html(builder.bindWithTable("table", 1).create());
    }
}
