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
import java.util.Date;
import java.util.List;
import pl.datamatica.traccar.api.dtos.out.ReportDto;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.reports.MapBuilder.MarkerStyle;

public class ReportGI extends ReportGenerator {
    @Override
    void generateImpl(ReportDto report) throws IOException, ProviderException {
        h2(report.getName());

        for (Device device : getDevices(report)) {
            Date from = getFromDate(report, device);
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
            if(!positions.isEmpty() && report.isIncludeMap()) {
                html("<div class=\"col-md-6\">");
            }
            dataTable(new Info(positions).calculate());
            if (!positions.isEmpty() && report.isIncludeMap()) {
                html("</div>");
                html("<div class=\"col-md-6\">");
                drawMap(positions);
                html("</div>");
            }
            panelBodyEnd();

            panelEnd();
        }
    }

    private void drawMap(List<Position> positions) {
        Position lastPosition = positions.get(positions.size()-1);
        MapBuilder builder = getMapBuilder()
                .polyline(positions, "#00f", 2)
                .marker(positions.get(0), MarkerStyle.routeStart())
                .marker(lastPosition, MarkerStyle.deviceMarker(lastPosition));
        html(builder.create());
    }

    static class Info {
        final List<Position> positions;
        Date start;
        Date end;
        double length;
        long moveDuration;
        long stopDuration;
        double topSpeed;
        double averageSpeed;
        int overspeedCount;

        Info(List<Position> positions) {
            this.positions = positions;
        }

        Info calculate() {
            this.start = positions.isEmpty() ? null : positions.get(0).getTime();
            this.end = positions.isEmpty() ? null : positions.get(positions.size() - 1).getTime();

            Position prevPosition = null;
            double totalSpeed = 0;
            int movingCount = 0;
            long nextStopDuration = 0;
            for (Position position : positions) {
                Device device = position.getDevice();
                this.length += position.getDistance();

                if (prevPosition != null) {
                    long diffTime = position.getTime().getTime() - prevPosition.getTime().getTime();
                    if (prevPosition.getSpeed() != null
                            && prevPosition.getSpeed() > device.getIdleSpeedThreshold()) {
                        moveDuration += diffTime;
                        // reclassify 'stop' duration into 'move' duration if the stop was less than setting from device profile
                        long minIdleTime = (long) device.getMinIdleTime() * 1000;
                        if (nextStopDuration > 0 && nextStopDuration < minIdleTime) {
                            stopDuration -= nextStopDuration;
                            moveDuration += nextStopDuration;
                        }
                        nextStopDuration = 0;
                    } else {
                        stopDuration += diffTime;
                        nextStopDuration += diffTime;
                    }
                }
                if (position.getSpeed() != null && position.getSpeed() > device.getIdleSpeedThreshold()) {
                    movingCount++;
                    totalSpeed += position.getSpeed();
                    topSpeed = Math.max(position.getSpeed(), topSpeed);
                }

                if (device.getSpeedLimit() != null
                        && position.getSpeed() != null && position.getSpeed() > device.getSpeedLimit()
                        && (prevPosition == null
                            || prevPosition.getSpeed() == null
                            || prevPosition.getSpeed() <= device.getSpeedLimit())) {
                    overspeedCount++;
                }

                prevPosition = position;
            }

            this.averageSpeed = movingCount == 0 ? 0 : totalSpeed / movingCount;

            return this;
        }
    }

    void dataTable(Info info) {
        tableStart();
        tableBodyStart();

        dataRow(message("report_route_start"), info.start == null ? "n/a" : formatDate(info.start));
        dataRow(message("report_route_end"), info.end == null ? "n/a" : formatDate(info.end));
        dataRow(message("report_route_length"), formatDistance(info.length));
        dataRow(message("report_move_duration"), formatDuration(info.moveDuration));
        dataRow(message("report_stop_duration"), formatDuration(info.stopDuration));
        dataRow(message("report_top_speed"), formatSpeed(info.topSpeed));
        dataRow(message("report_average_speed"), formatSpeed(info.averageSpeed));
        dataRow(message("report_overspeed_count"), Integer.toString(info.overspeedCount));

        tableBodyEnd();
        tableEnd();
    }
}
