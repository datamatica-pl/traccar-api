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

import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Position;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import pl.datamatica.traccar.api.dtos.out.ReportDto;
import pl.datamatica.traccar.api.providers.ProviderException;

public class ReportOS extends ReportGenerator {
    @Override
    void generateImpl(ReportDto report) throws IOException, ProviderException {
        h2(report.getName());

        for (Device device : getDevices(report)) {
            Date from = getFromDate(report, device);
            List<Position> positions;
            if (device.getSpeedLimit() == null) {
                positions = Collections.emptyList();
            } else {
                positions = getPositions(device, 
                        from, report.getToDate(), report.isDisableFilter());
            }

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
            if (!positions.isEmpty()) {
                if(report.isIncludeMap())
                    html("<div class=\"col-md-6\">");
                drawTable(positions);
                if(report.isIncludeMap()) {
                    html("</div");
                    html("<div class=\"col-md-6\">");
                    drawMap(positions);
                    html("</div>");
                }
            }

            panelBodyEnd();

            panelEnd();
        }
    }

    void drawTable(List<Position> positions) {
        tableStart("table", hover().condensed().height(DEFAULT_TABLE_HEIGHT));

        // header
        tableHeadStart();
        tableRowStart();

        for (String header : new String[] {"start", "end", "duration", "top_speed", "average_speed", "overspeed_position"}) {
            tableHeadCellStart();
            text(message("report_"+header));
            tableHeadCellEnd();
        }

        tableRowEnd();
        tableHeadEnd();

        // body
        tableBodyStart();

        Position start = null;
        double topSpeed = 0;
        double speedSUM = 0;
        int overspeedPositionCount = 0;

        for (Iterator<Position> it = positions.iterator(); it.hasNext(); ) {
            Position position = it.next();

            if (!it.hasNext() && start == null && isOverspeed(position)) {
                start = position;
            }

            if (start != null && (!it.hasNext() || !isOverspeed(position))) {
                tableRowStart();
                tableCell(formatDate(start.getTime()));
                tableCell(formatDate(position.getTime()));
                long duration = position.getTime().getTime() - start.getTime().getTime();
                tableCell(formatDuration(duration));
                tableCell(formatSpeed(topSpeed));
                tableCell(formatSpeed(speedSUM / overspeedPositionCount));
                tableCell(formatSpeed(start.getSpeed()));
                extentCell(start, start);
                tableRowEnd();

                // reset counters
                start = null;
                topSpeed = 0;
                speedSUM = 0;
                overspeedPositionCount = 0;
            }

            if (isOverspeed(position)) {
                if (start == null) {
                    start = position;
                }
                topSpeed = Math.max(topSpeed, position.getSpeed());
                speedSUM += position.getSpeed();
                overspeedPositionCount++;
            }
        }

        tableBodyEnd();

        tableEnd();
    }

    private boolean isOverspeed(Position position) {
        return position.getSpeed() > position.getDevice().getSpeedLimit();
    }

    private void drawMap(List<Position> positions) {
        MapBuilder builder = getMapBuilder();
        for(Position p : positions) {
            if(isOverspeed(p))
                builder.marker(p, MapBuilder.MarkerStyle.arrow(p.getCourse()));
        }
        html(builder.bindWithTable("table", 1).create());
    }
}
