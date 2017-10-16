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
import java.util.*;
import java.util.stream.Collectors;
import pl.datamatica.traccar.api.dtos.out.ReportDto;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.utils.GeoUtils;

public class ReportMD extends ReportGenerator {
    @Override
    void generateImpl(ReportDto report) throws IOException, ProviderException {
        h2(report.getName());

        for (Device device : getDevices(report)) {
            List<Position> positions = positionProvider.getAllAvailablePositions(
                    device, report.getFromDate(), report.getToDate(), 0)
                    .collect(Collectors.toList());
            for(int i=1;i<positions.size();++i) {
                Position prev = positions.get(i-1),
                        cur = positions.get(i);
                cur.setDistance(GeoUtils.getDistance(prev.getLongitude(), 
                        prev.getLatitude(), cur.getLongitude(), cur.getLatitude()));
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
            text(formatDate(report.getFromDate()) + " - " + formatDate(report.getToDate()));
            paragraphEnd();
            // device details
            deviceDetails(device);
            // data table
            if (!positions.isEmpty()) {
                drawTable(positions);
            }

            panelBodyEnd();

            panelEnd();
        }
    }

    void drawTable(List<Position> positions) {
        tableStart(hover().condensed());

        // header
        tableHeadStart();
        tableRowStart();

        for (String header : new String[] {"date", "start", "end", "distance"}) {
            tableHeadCellStart();
            text(message("report_"+header));
            tableHeadCellEnd();
        }

        tableRowEnd();
        tableHeadEnd();

        // body
        tableBodyStart();

        Position start = null;
        Position end = null;
        Position prevPosition = null;
        double totalDistance = 0;
        double dailyDistance = 0;

        for (Iterator<Position> it = positions.iterator(); it.hasNext(); ) {
            Position position = it.next();

            if (!it.hasNext() || (prevPosition != null && isNextDay(prevPosition, position))) {
                if (dailyDistance > 0) {
                    tableRowStart();
                    tableCell(formatDateLong(prevPosition == null ? position.getTime() : prevPosition.getTime()));
                    tableCell(start == null ? "" : formatDate(start.getTime()));
                    tableCell(end == null ? "" : formatDate(end.getTime()));
                    tableCell(formatDistance(dailyDistance));
                    tableRowEnd();
                }

                // reset counters
                start = null;
                end = null;
                dailyDistance = 0;
            }

            if (isMoving(position)) {
                if (start == null) {
                    start = position;
                }
                end = position;
            }
            dailyDistance += position.getDistance();
            totalDistance += position.getDistance();
            prevPosition = position;
        }

        tableBodyEnd();

        tableEnd();

        paragraphStart();
        bold(message("report_total_mileage") + ": " + formatDistance(totalDistance));
    }

    boolean isNextDay(Position prevPosition, Position position) {
        Calendar prev = Calendar.getInstance(getTimeZone());
        prev.setTime(prevPosition.getTime());
        Calendar curr = Calendar.getInstance(getTimeZone());
        curr.setTime(position.getTime());
        return prev.get(Calendar.DAY_OF_MONTH) != curr.get(Calendar.DAY_OF_MONTH);
    }

    boolean isMoving(Position position) {
        return position.getSpeed() != null && position.getSpeed() > position.getDevice().getIdleSpeedThreshold();
    }
}
