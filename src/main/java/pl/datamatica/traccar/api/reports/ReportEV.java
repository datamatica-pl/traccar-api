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

import pl.datamatica.traccar.model.Maintenance;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.DeviceEvent;
import pl.datamatica.traccar.model.Device;

import java.io.IOException;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pl.datamatica.traccar.api.dtos.out.ReportDto;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.reports.MapBuilder.MarkerStyle;
import pl.datamatica.traccar.model.DeviceEventType;
import static pl.datamatica.traccar.model.DeviceEventType.*;

public class ReportEV extends ReportGenerator {
    @Override
    void generateImpl(ReportDto report) throws IOException, ProviderException {
        h2(report.getName());

        for (Device device : getDevices(report)) {
            Date from = getFromDate(report, device);
            List<DeviceEvent> events = entityManager.createQuery("SELECT e FROM DeviceEvent e" +
                    " INNER JOIN FETCH e.position" +
                    " WHERE e.device=:device AND e.time BETWEEN :from AND :to "
                    + "AND e.type in (:validTypes)"
                    + "ORDER BY e.time", DeviceEvent.class)
                    .setParameter("device", device)
                    .setParameter("from", from)
                    .setParameter("to", report.getToDate())
                    .setParameter("validTypes", EnumSet.of(GEO_FENCE_ENTER, GEO_FENCE_EXIT, OVERSPEED))
                    .getResultList();
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
            if (!events.isEmpty()) {
                if(report.isIncludeMap()) {
                    html("<div class=\"col-md-6\">");
                }
                drawTable(getGeoFences(report, device), events);
                if(report.isIncludeMap()) {
                    html("</div>");
                    html("<div class=\"col-md-6\">");
                    drawMap(getGeoFences(report, device), events);
                    html("</div>");
                }
            }

            panelBodyEnd();

            panelEnd();

        }
    }
    
    private void drawMap(List<GeoFence> gfs, List<DeviceEvent> events) {
        MapBuilder builder = getMapBuilder();
        for(DeviceEvent ev : events) {
            if (ev.getGeoFence() != null && !gfs.contains(ev.getGeoFence())) {
                continue;
            }
            if(isVisible(ev))
                builder.marker(ev.getPosition(), 
                        MarkerStyle.event(ev.getType(), ""));
        }
        for(GeoFence gf : gfs)
            builder.geofence(gf);
        html(builder.bindWithTable("table", 1).create());
    }

    private boolean isVisible(DeviceEvent ev) {
        return ev.getType() == DeviceEventType.GEO_FENCE_ENTER 
                || ev.getType() == DeviceEventType.GEO_FENCE_EXIT
                || ev.getType() == DeviceEventType.OVERSPEED;
                
    }
    
    static class Stats {
        int offline;
        Map<GeoFence, Integer> geoFenceEnter = new HashMap<>();
        Map<GeoFence, Integer> geoFenceExit = new HashMap<>();
        Map<Maintenance, Integer> maintenances = new HashMap<>();

        void update(DeviceEvent event) {
            switch (event.getType()) {
                case GEO_FENCE_ENTER:
                    update(geoFenceEnter, event.getGeoFence());
                    break;
                case GEO_FENCE_EXIT:
                    update(geoFenceExit, event.getGeoFence());
                    break;
                case OFFLINE:
                    offline++;
                    break;
                case MAINTENANCE_REQUIRED:
                    update(maintenances, event.getMaintenance());
                    break;
            }
        }

        <T> void update(Map<T, Integer> map, T entity) {
            if (entity != null) {
                Integer current = map.get(entity);
                map.put(entity, current == null ? 1 : (current + 1));
            }
        }
    }

    void drawTable(List<GeoFence> geoFences, List<DeviceEvent> events) {
        tableStart("table", hover().condensed().height(DEFAULT_TABLE_HEIGHT));

        // header
        tableHeadStart();
        tableRowStart();

        for (String header : new String[] {"time", "event"}) {
            tableHeadCellStart();
            text(message("report_"+header));
            tableHeadCellEnd();
        }

        tableHeadEnd();

        Stats stats = new Stats();
        // body
        tableBodyStart();

        for (DeviceEvent event : events) {
            if (event.getGeoFence() != null && !geoFences.contains(event.getGeoFence())) {
                continue;
            }

            tableRowStart();
            tableCell(formatDate(event.getTime()));
            String eventText = message("report_event_" + event.getType().name().toLowerCase());
            if (event.getGeoFence() != null) {
                eventText += " (" + event.getGeoFence().getName() + ")";
            }
            if (event.getMaintenance() != null) {
                eventText += " (" + event.getMaintenance().getName() + ")";
            }
            if(event.getType() == DeviceEventType.OVERSPEED) {
                eventText += " (" + formatSpeed(event.getPosition().getSpeed()) +")";
            }
            tableCell(eventText);
            extentCell(event.getPosition(), event.getPosition());
            tableRowEnd();

            stats.update(event);
        }

        tableBodyEnd();
        tableEnd();

        // summary
        tableStart();
        tableBodyStart();

        if (stats.offline > 0) {
            dataRow(message("report_total_offline"), Integer.toString(stats.offline));
        }
        for (GeoFence geoFence : geoFences) {
            Integer enterCount = stats.geoFenceEnter.get(geoFence);
            if (enterCount != null) {
                dataRow(message("report_total_geofence_enters") + " (" + geoFence.getName() + ")", enterCount.toString());
            }
        }
        for (GeoFence geoFence : geoFences) {
            Integer enterCount = stats.geoFenceExit.get(geoFence);
            if (enterCount != null) {
                dataRow(message("report_total_geofence_exits") + " (" + geoFence.getName() + ")", enterCount.toString());
            }
        }
        if (!stats.maintenances.isEmpty()) {
            for (Map.Entry<Maintenance, Integer> entry : stats.maintenances.entrySet()) {
                dataRow(message("report_total_maintenance_required") + " (" + entry.getKey().getName() + ")", entry.getValue().toString());
            }
        }

        tableBodyEnd();
        tableEnd();
    }
}
