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

import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserSettings;
import pl.datamatica.traccar.model.ReportFormat;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.Device;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import pl.datamatica.traccar.api.dtos.out.ReportDto;
import pl.datamatica.traccar.api.metadata.model.DeviceIcon;
import pl.datamatica.traccar.api.providers.DeviceIconProvider;
import pl.datamatica.traccar.api.providers.DeviceProvider;
import pl.datamatica.traccar.api.providers.GeoFenceProvider;
import pl.datamatica.traccar.api.providers.MessageProvider;
import pl.datamatica.traccar.api.providers.PositionProvider;
import pl.datamatica.traccar.api.providers.PositionProvider.PositionSpeedOperator;
import pl.datamatica.traccar.api.providers.PositionProvider.PositionsQueryParams;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.utils.GeoUtils;

public abstract class ReportGenerator {
    public static final int DEFAULT_TABLE_HEIGHT = 150;
    
    EntityManager entityManager;
    PositionProvider positionProvider;
    DeviceProvider deviceProvider;
    GeoFenceProvider gfProvider;
    MessageProvider msgProvider;
    User currentUser;
    ApplicationSettings applicationSettings;
    
    private IReportRenderer renderer;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat longDateFormat;
    private TimeZone timeZone;
    private DeviceIconProvider iconsProvider;
    private Map<Long, String> icons;
    
    abstract void generateImpl(ReportDto report) throws IOException, ProviderException;

    public final String generate(ReportDto report, HttpServletResponse response, String lang) throws IOException, ProviderException {
        StringWriter writer = new StringWriter();
        if(ReportFormat.valueOf(report.getFormat()) == ReportFormat.CSV)
            renderer = new CSVReportRenderer(response, new PrintWriter(writer));
        else
            renderer = new HtmlReportRenderer(response, new PrintWriter(writer));

        timeZone = currentUser.getUserSettings().getTimeZoneId() == null
                ? TimeZone.getDefault()
                : TimeZone.getTimeZone(currentUser.getUserSettings().getTimeZoneId());
        if(lang == null)
            lang = applicationSettings.getLanguage();
        Locale locale = new Locale(lang);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
        dateFormat.setTimeZone(timeZone);
        longDateFormat = new SimpleDateFormat("d MMM yyyy", locale);
        longDateFormat.setTimeZone(timeZone);
        
        loadDeviceIcons();
        renderer.start(report);
        generateImpl(report);
        renderer.end(report);
        
        return writer.toString();
    }

    private void loadDeviceIcons() throws IOException {
        icons = new HashMap<>();
        for(DeviceIcon di : iconsProvider.getDeviceIconsMetadata())
            icons.put(di.getId(), di.getIconUrl().replace("/images/", "/markers/"));
    }
    
    protected List<Position> getPositions(Device d, Date from, Date to, boolean disableFilter) throws ProviderException {
        List<Position> positions = positionProvider.getAllAvailablePositions(d, 
                    from, to, 0)
                .sorted((p1, p2) -> p1.getTime().compareTo(p2.getTime()))
                .collect(Collectors.toList());
        if(!disableFilter) {
            UserSettings us = currentUser.getUserSettings();
            PositionsQueryParams qp = new PositionsQueryParams();
            qp.hideZero = us.isHideZeroCoordinates();
            qp.hideInvalid = us.isHideInvalidLocations();
            qp.hideDuplicates = us.isHideDuplicates();
            if(us.getSpeedForFilter() != null) {
                qp.speedValue = us.getSpeedForFilter().intValue();
                qp.speedComp = speedOpFromSpeedModifier(us.getSpeedModifier());
            }
            PositionProvider.filterPositions(positions, qp);
        }
        for(int i=1;i<positions.size();++i) {
            Position current=positions.get(i),
                    previous = positions.get(i-1);
            current.setDistance(GeoUtils.getDistance(previous.getLongitude(), 
                    previous.getLatitude(), current.getLongitude(), current.getLatitude()));
        }
        return positions;
    }
    
    private PositionSpeedOperator speedOpFromSpeedModifier(String modifier) {
        if("<".equals(modifier))
            return PositionSpeedOperator.LESS;
        else if("<=".equals(modifier))
            return PositionSpeedOperator.LESSEQUAL;
        else if("=".equals(modifier))
            return PositionSpeedOperator.EQUAL;
        else if(">=".equals(modifier))
            return PositionSpeedOperator.GREATEREQUAL;
        else if(">".equals(modifier))
            return PositionSpeedOperator.GREATER;
        throw new IllegalArgumentException();
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void setPositionProvider(PositionProvider positionProvider) {
        this.positionProvider = positionProvider;
    }

    public void setDeviceProvider(DeviceProvider deviceProvider) {
        this.deviceProvider = deviceProvider;
    }

    public void setGfProvider(GeoFenceProvider gfProvider) {
        this.gfProvider = gfProvider;
    }

    public void setMsgProvider(MessageProvider msgProvider) {
        this.msgProvider = msgProvider;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setIconsProvider(DeviceIconProvider iconsProvider) {
        this.iconsProvider = iconsProvider;
    }

    
    
    
    void h1(String text) {
        renderer.h1(text);
    }

    void h2(String text) {
        renderer.h2(text);
    }

    void h3(String text) {
        renderer.h3(text);
    }

    public void tableRowStart() {
        renderer.tableRowStart();
    }

    public void paragraphEnd() {
        renderer.paragraphEnd();
    }

    public void tableRowEnd() {
        renderer.tableRowEnd();
    }

    public void tableBodyEnd() {
        renderer.tableBodyEnd();
    }

    public void tableStart() {
        renderer.tableStart("", null);
    }

    public void tableStart(IReportRenderer.TableStyle style) {
        renderer.tableStart("", style);
    }
    
    public void tableStart(String id, IReportRenderer.TableStyle style) {
        renderer.tableStart(id, style);
    }

    IReportRenderer.TableStyle hover() {
        return new HtmlReportRenderer.TableStyle().hover();
    }

    IReportRenderer.TableStyle condensed() {
        return new HtmlReportRenderer.TableStyle().condensed();
    }

    public void tableHeadStart() {
        renderer.tableHeadStart();
    }

    public void tableHeadEnd() {
        renderer.tableHeadEnd();
    }

    public void tableHeadCellStart() {
        renderer.tableHeadCellStart(null);
    }

    public void tableHeadCellStart(IReportRenderer.CellStyle style) {
        renderer.tableHeadCellStart(style);
    }

    public void tableHeadCellEnd() {
        renderer.tableHeadCellEnd();
    }

    public void panelBodyStart() {
        renderer.panelBodyStart();
    }

    public void panelBodyEnd() {
        renderer.panelBodyEnd();
    }

    public void tableCellEnd() {
        renderer.tableCellEnd();
    }

    public void panelStart() {
        renderer.panelStart();
    }

    public void panelHeadingEnd() {
        renderer.panelHeadingEnd();
    }

    public void text(String text) {
        renderer.text(text);
    }

    public void tableEnd() {
        renderer.tableEnd();
    }

    public void panelEnd() {
        renderer.panelEnd();
    }

    public void panelHeadingStart() {
        renderer.panelHeadingStart();
    }

    public void tableBodyStart() {
        renderer.tableBodyStart();
    }
    
    public void paragraphStart() {
        renderer.paragraphStart();
    }

    public void bold(String text) {
        renderer.bold(text);
    }

    public void tableCellStart() {
        renderer.tableCellStart(null);
    }

    void tableCell(String text) {
        tableCellStart();
        text(text);
        tableCellEnd();
    }
    
    void extentCell(Position p1, Position p2) {
        String extent = new MapBuilder.MapBoundsBuilder()
                .addPosition(p1)
                .addPosition(p2)
                .create(1e-2);
        tableCellStart(new HtmlReportRenderer.CellStyle().hidden(true).id("ext"));
        text(extent);
        tableCellEnd();
    }

    public void tableCellStart(IReportRenderer.CellStyle style) {
        renderer.tableCellStart(style);
    }

    public void link(String url, String target, String text) {
        renderer.link(url, target, text);
    }

    void mapLink(double latitude, double longitude) {
        UserSettings userSettings = currentUser.getUserSettings();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat lonLatFormat = new DecimalFormat("##.######", symbols);
        String text = lonLatFormat.format(latitude) + " \u00B0, " +
                lonLatFormat.format(longitude) + " \u00B0";

        link("http://www.openstreetmap.org/?" +
                "mlat=" + lonLatFormat.format(latitude) + "&mlon=" + lonLatFormat.format(longitude) +
                "#map=" + userSettings.getZoomLevel() + "/" +
                lonLatFormat.format(latitude) + "/" + lonLatFormat.format(longitude),
                "_blank", text);
    }
    
    protected MapBuilder getMapBuilder() {
        return new MapBuilder("100%", "500px", icons);
    }
    
    void html(String html) {
        renderer.html(html);
    }
    
    void dataRow(String title, String text) {
        tableRowStart();
        tableCellStart();
        bold(title + ":");
        tableCellEnd();
        tableCellStart();
        text(text);
        tableCellEnd();
        tableRowEnd();
    }

    void deviceDetails(Device device) {
        if (hasNonEmpty(device.getDescription(), device.getPlateNumber(), device.getVehicleInfo())) {
            paragraphStart();
            tableStart();
            tableBodyStart();

            if (isNotEmpty(device.getDescription())) {
                dataRow(message("report_device_description"), device.getDescription());
            }

            if (isNotEmpty(device.getPlateNumber())) {
                dataRow(message("report_plate_number"), device.getPlateNumber());
            }

            if (isNotEmpty(device.getVehicleInfo())) {
                dataRow(message("report_vehicle_brand_model_color"), device.getVehicleInfo());
            }

            tableBodyEnd();
            tableEnd();
            paragraphEnd();
        }
    }

    boolean hasNonEmpty(String... strings) {
        for (String string : strings) {
            if (isNotEmpty(string)) {
                return true;
            }
        }
        return false;
    }

    boolean isNotEmpty(String string) {
        return string != null && !string.trim().isEmpty();
    }

    IReportRenderer.CellStyle colspan(int colspan) {
        return new HtmlReportRenderer.CellStyle().colspan(colspan);
    }

    IReportRenderer.CellStyle rowspan(int rowspan) {
        return new HtmlReportRenderer.CellStyle().rowspan(rowspan);
    }

    List<Device> getDevices(ReportDto report) throws ProviderException {
        if (report.getDeviceIds().isEmpty()) {
            return deviceProvider.getAllAvailableDevices().collect(Collectors.toList());
        } else {
            List<Device> devices = new ArrayList<>(report.getDeviceIds().size());
            for (long id : report.getDeviceIds()) {
                devices.add(deviceProvider.getDevice(id));
            }
            return devices;
        }
    }

    List<GeoFence> getGeoFences(ReportDto report, Device device) throws ProviderException {
        List<GeoFence> geoFences;
        if (report.getGeofenceIds().isEmpty()) {
            geoFences = gfProvider.getAllAvailableGeoFences().collect(Collectors.toList());
        } else {
            geoFences = new ArrayList<>(report.getGeofenceIds().size());
            for (long id : report.getGeofenceIds()) {
                geoFences.add(gfProvider.getGeoFence(id));
            }
        }
        // filter device-specific geo-fences that are not assigned to device from method arguments
        for (Iterator<GeoFence> it = geoFences.iterator(); it.hasNext(); ) {
            GeoFence geoFence = it.next();
            if (!geoFence.getDevices().contains(device)) {
                it.remove();
            }
        }
        return geoFences;
    }

    String formatDuration(long duration) {
        if (duration == 0) {
            return "0s";
        }

        int days = (int) (duration / 86400000L);
        duration -= (long) days * 86400000L;

        int hours = (int) (duration / 3600000L);
        duration -= (long) hours * 3600000L;

        int minutes = (int) (duration / 60000L);
        duration -= (long) minutes * 60000L;

        int seconds = (int) (duration / 1000L);

        return
                (days == 0 ? "" : days + message("report_day") + " ") +
                        (hours == 0 ? "" : hours + message("report_hour") + " ") +
                        (minutes == 0 ? "" : minutes + message("report_minute") + " ") +
                        (seconds == 0 ? "" : seconds + message("report_second") + " ");
    }

    String formatSpeed(double speed) {
        UserSettings.SpeedUnit speedUnit = currentUser.getUserSettings().getSpeedUnit();
        NumberFormat speedFormat = NumberFormat.getInstance();
        speedFormat.setMaximumFractionDigits(2);
        speedFormat.setMinimumIntegerDigits(0);
        return speedFormat.format((Double.isNaN(speed) ? 0d : speed) * speedUnit.getFactor()) + " " + speedUnit.getUnit();
    }

    String formatDistance(double distance) {
        UserSettings.SpeedUnit speedUnit = currentUser.getUserSettings().getSpeedUnit();
        UserSettings.DistanceUnit distanceUnit = speedUnit.getDistanceUnit();
        NumberFormat distanceFormat = NumberFormat.getInstance();
        distanceFormat.setMaximumFractionDigits(2);
        distanceFormat.setMinimumIntegerDigits(0);
        distanceFormat.setMinimumIntegerDigits(1);
        return distanceFormat.format((Double.isNaN(distance) ? 0d : distance) * distanceUnit.getFactor()) + " " + distanceUnit.getUnit();
    }

    String formatDate(Date date) {
        return dateFormat.format(date);
    }

    String formatDateLong(Date date) {
        return longDateFormat.format(date);
    }

    String message(String key) {
        return msgProvider.getMessage(key);
    }

    TimeZone getTimeZone() {
        return timeZone;
    }
}
