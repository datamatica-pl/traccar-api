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
package pl.datamatica.traccar.api.reports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import pl.datamatica.traccar.api.dtos.out.ReportDto;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.utils.GeoFenceCalculator;
import pl.datamatica.traccar.model.DbRoute;
import pl.datamatica.traccar.model.DeviceEvent;
import pl.datamatica.traccar.model.DeviceEventType;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.Route;
import pl.datamatica.traccar.model.RoutePoint;

/**
 *
 * @author ŁŁ
 */
//test on !test! 2017-06-06 ~RealDevice
public class ReportTrack extends ReportGenerator{

    @Override
    void generateImpl(ReportDto report) throws IOException, ProviderException {
        DbRoute route = entityManager.find(DbRoute.class, report.getRouteId());
        Date from = getFromDate(report, route.getDevice());
        
        h2(report.getName());
        bold("Trasa: ");
        text(route.getName());
        paragraphStart();
        bold(message("report_time_period") + ": ");
        text(formatDate(from) + " - " + formatDate(report.getToDate()));
        paragraphEnd();
        
        List<RoutePoint> rp = route.getRoutePoints();
        if(route.getStatus() == Route.Status.NEW)
            return;
        Date startTime = rp.get(0).getEnterTime(),
                endTime = rp.get(0).getExitTime();
        for(RoutePoint pt : rp) {
            if(startTime == null || pt.getEnterTime().before(startTime))
                startTime = pt.getEnterTime();
            if(endTime == null || pt.getExitTime().after(endTime))
                endTime = pt.getExitTime();
        }
        if(startTime.before(from))
            startTime = from;
        if(endTime == null)
            endTime = report.getToDate();
        List<Position> history = positionProvider.getDeviceHistory(
                route.getDevice(), startTime, endTime)
                .collect(Collectors.toList());
        
        Set<GeoFence> gfs = new HashSet<>();
        for(RoutePoint p : rp)
            gfs.add(p.getGeofence());
        
        List<DeviceEvent> rpe = calculate(gfs, history);
        if(report.isIncludeMap())
            html("<div class=\"col-md-6\">");
        drawTable("rpe", rpe);
        List<DeviceEvent> alle = new ArrayList<>(rpe);
        
        if(route.getCorridor() != null) {
            List<DeviceEvent> core = calculate(Collections.singleton(route.getCorridor()), history);
            drawTable("core", core);
            alle.addAll(core);
        }
        
        if(report.isIncludeMap() && !alle.isEmpty()) {
            html("</div>");
            html("<div class=\"col-md-6\">");
            drawMap(alle, gfs);
            html("</div>");
        }
    }
    
    void drawTable(String id, List<DeviceEvent> datas) {

        // draw
        tableStart(id, hover().condensed().height(250));

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
    
    List<DeviceEvent> calculate(Set<GeoFence> geoFences, List<Position> positions) {
        // calculate
        GeoFenceCalculator calculator = new GeoFenceCalculator(geoFences);
        Set<GeoFence> currentData = new HashSet<>(geoFences.size());
        List<DeviceEvent> result = new ArrayList<>();

        for (Position position : positions) {
            for (GeoFence geoFence : geoFences) {
                if (calculator.contains(geoFence, position, true)) {
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

    void drawMap(List<DeviceEvent> events, Collection<GeoFence> gfs) {
        MapBuilder builder = getMapBuilder();
        for(DeviceEvent ev : events) {
            builder.marker(ev.getPosition(), 
                    MapBuilder.MarkerStyle.event(ev.getType(), ""));
        }
        for(GeoFence gf : gfs)
            builder.geofence(gf);
        builder.bindWithTable("rpe", 1)
                .bindWithTable("core", 1);
        html(builder.create());
    }
}
