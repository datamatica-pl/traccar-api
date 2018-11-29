/*
 *  Copyright (C) 2018  Datamatica (dev@datamatica.pl)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published
 *  by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pl.datamatica.traccar.api.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pl.datamatica.traccar.api.dtos.out.DeviceDto;
import pl.datamatica.traccar.api.dtos.out.ListDto;
import pl.datamatica.traccar.api.providers.ApplicationSettingsProvider;
import pl.datamatica.traccar.api.providers.DeviceProvider;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.responses.*;
import pl.datamatica.traccar.api.utils.DateUtil;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;

/**
 *
 * @author Jan Usarek
 */
public class GetModifiedDataTest {

    private User user;
    private DeviceProvider dp;
    private DevicesController dc;
    private ApplicationSettings settings;
    private RequestContext rc;
    private List<Device> devices = new ArrayList<>();
    private final TempDeviceFactory tempDeviceFactory = new TempDeviceFactory();

    @Before
    public void testInit() throws ProviderException {
        user = new User();
        rc = Mockito.mock(RequestContext.class);
        dp = Mockito.mock(DeviceProvider.class);
        settings = new ApplicationSettings();
        ApplicationSettingsProvider asp = Mockito.mock(ApplicationSettingsProvider.class);
        Mockito.when(asp.get()).thenReturn(settings);
        Mockito.when(rc.getDeviceProvider()).thenReturn(dp);
        Mockito.when(rc.getUser()).thenReturn(user);
        Mockito.when(rc.getApplicationSettingsProvider()).thenReturn(asp);
        dc = new DevicesController(rc);

        this.initializeTestDevices();

        Mockito.when(dp.getDevice(0)).thenReturn(devices.get(0));
        Mockito.when(dp.getAllAvailableDevices()).thenReturn(devices.stream());
    }

    private void initializeTestDevices() {
        try {
            // Three devices with last modification time "2018-11-01 16:00:00 GMT" but in different TZs
            devices.add(tempDeviceFactory.get(1L, "1001", user, "2018-11-01 16:00:00 GMT"));
            devices.add(tempDeviceFactory.get(2L, "1002", user, "2018-11-01 08:00:00 PST"));
            devices.add(tempDeviceFactory.get(3L, "1003", user, "2018-11-01 17:00:00 CET"));

            // Three devices with last modification time "2018-11-10 18:00:00 GMT" but in different TZs
            // CEST timezone is not active in November, but it should work
            devices.add(tempDeviceFactory.get(4L, "2001", user, "2018-11-10 10:00:00 PST"));
            devices.add(tempDeviceFactory.get(5L, "2002", user, "2018-11-10 18:00:00 GMT"));
            devices.add(tempDeviceFactory.get(6L, "2003", user, "2018-11-10 20:00:00 CEST"));

            // Three devices with last modification time "2018-11-20 20:00:00 GMT" but in different TZs
            // PDT timezone is not active in November, but it should work
            devices.add(tempDeviceFactory.get(7L, "1236", user, "2018-11-20 13:00:00 PDT"));
            devices.add(tempDeviceFactory.get(8L, "1236", user, "2018-11-20 20:00:00 GMT"));
            devices.add(tempDeviceFactory.get(9L, "1236", user, "2018-11-20 21:00:00 CET"));
        } catch (ParseException pe) {
            fail("Device list cannot be created because of incorrect modification time.");
        }
    }

    @Test
    public void getSixModifiedDevicesZoneGMT() throws Exception {
        Date ifModifiedSince = DateUtil.parseDate("Thu, 01 Nov 2018 16:00:01 GMT");
        Mockito.when(rc.getModificationDate()).thenReturn(ifModifiedSince);

        HttpResponse response = dc.get();

        ListDto<DeviceDto> actual = (ListDto<DeviceDto>) response.getContent();
        assertEquals(6, actual.getChanged().size());
    }

    @Test
    public void getNineModifiedDevicesZoneGMT() throws Exception {
        Date ifModifiedSince = DateUtil.parseDate("Thu, 01 Nov 2018 15:59:59 GMT");
        Mockito.when(rc.getModificationDate()).thenReturn(ifModifiedSince);

        HttpResponse response = dc.get();

        ListDto<DeviceDto> actual = (ListDto<DeviceDto>) response.getContent();
        assertEquals(9, actual.getChanged().size());
    }

    @Test
    public void getSixModifiedDevicesZonePST() throws Exception {
        // "Sat, 10 Nov 2018 09:59:59 PST" -> 17:59:59 GMT
        Date ifModifiedSince = DateUtil.parseDate("Sat, 10 Nov 2018 09:59:59 PST");
        Mockito.when(rc.getModificationDate()).thenReturn(ifModifiedSince);

        HttpResponse response = dc.get();

        ListDto<DeviceDto> actual = (ListDto<DeviceDto>) response.getContent();
        assertEquals(6, actual.getChanged().size());
    }

    @Test
    public void getThreeModifiedDevicesZonePST() throws Exception {
        // "Sat, 10 Nov 2018 10:00:01 PST" -> 18:01:01 GMT
        Date ifModifiedSince = DateUtil.parseDate("Sat, 10 Nov 2018 10:00:01 PST");
        Mockito.when(rc.getModificationDate()).thenReturn(ifModifiedSince);

        HttpResponse response = dc.get();

        ListDto<DeviceDto> actual = (ListDto<DeviceDto>) response.getContent();
        assertEquals(3, actual.getChanged().size());
    }

    @Test
    public void getThreeModifiedDevicesZoneCET() throws Exception {
        Date ifModifiedSince = DateUtil.parseDate("Tue, 20 Nov 2018 20:59:59 CET"); // 19:59:59 GMT
        Mockito.when(rc.getModificationDate()).thenReturn(ifModifiedSince);

        HttpResponse response = dc.get();

        ListDto<DeviceDto> actual = (ListDto<DeviceDto>) response.getContent();
        assertEquals(3, actual.getChanged().size());
    }

    @Test
    public void getZeroModifiedDevicesZoneCET() throws Exception {
        Date ifModifiedSince = DateUtil.parseDate("Tue, 20 Nov 2018 21:01:01 CET"); // 20:00:01 GMT
        Mockito.when(rc.getModificationDate()).thenReturn(ifModifiedSince);

        HttpResponse response = dc.get();

        ListDto<DeviceDto> actual = (ListDto<DeviceDto>) response.getContent();
        assertEquals(0, actual.getChanged().size());
    }

    // Check CEST, even if not available in November, it should also work.
    @Test
    public void getSixModifiedDevicesZoneCEST() throws Exception {
        Date ifModifiedSince = DateUtil.parseDate("Sat, 10 Nov 2018 19:59:59 CEST"); // 17:59:59 GMT
        Mockito.when(rc.getModificationDate()).thenReturn(ifModifiedSince);

        HttpResponse response = dc.get();

        ListDto<DeviceDto> actual = (ListDto<DeviceDto>) response.getContent();
        assertEquals(6, actual.getChanged().size());
    }

    // Check CEST, even if not available in November, it should also work.
    @Test
    public void getThreeModifiedDevicesZoneCEST() throws Exception {
        Date ifModifiedSince = DateUtil.parseDate("Sat, 10 Nov 2018 20:00:01 CEST"); // 18:00:01 GMT
        Mockito.when(rc.getModificationDate()).thenReturn(ifModifiedSince);

        HttpResponse response = dc.get();

        ListDto<DeviceDto> actual = (ListDto<DeviceDto>) response.getContent();
        assertEquals(3, actual.getChanged().size());
    }

    @Test
    public void getThreeModifiedDevicesExactTimeZoneCEST() throws Exception {
        Date ifModifiedSince = DateUtil.parseDate("Sat, 10 Nov 2018 20:00:00 CEST"); // 18:00:01 GMT
        Mockito.when(rc.getModificationDate()).thenReturn(ifModifiedSince);

        HttpResponse response = dc.get();

        ListDto<DeviceDto> actual = (ListDto<DeviceDto>) response.getContent();
        assertEquals(3, actual.getChanged().size());
    }

    @Test
    public void checkMinDateForPostionsFiltering() throws Exception {
        Date ifModifiedSince = DateUtil.parseDate("Tue, 27 Nov 2018 8:00:00 PST");
        Mockito.when(rc.getModificationDate()).thenReturn(ifModifiedSince);
        DevicesController dc1 = new DevicesController(rc);
        
        Mockito.when(rc.getModificationDate()).thenReturn(DateUtil.parseDate("Tue, 27 Nov 2018 09:00:00 PDT"));
        DevicesController dc2 = new DevicesController(rc);
        
        Mockito.when(rc.getModificationDate()).thenReturn(DateUtil.parseDate("Tue, 27 Nov 2018 16:00:00 GMT"));
        DevicesController dc3 = new DevicesController(rc);
        
        Mockito.when(rc.getModificationDate()).thenReturn(DateUtil.parseDate("Tue, 27 Nov 2018 17:00:00 CET"));
        DevicesController dc4 = new DevicesController(rc);
        
        Mockito.when(rc.getModificationDate()).thenReturn(DateUtil.parseDate("Tue, 27 Nov 2018 18:00:00 CEST"));
        DevicesController dc5 = new DevicesController(rc);
        
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss z");
        Date dateToCheck = format.parse("2018-11-27 16:00:00 GMT");
        
        // Make sure, than no matter of TimeZone of date used in If-modified-since,
        // correct date and time is used for filtering positions
        assertEquals(dateToCheck, dc1.getMinDate());
        assertEquals(dateToCheck, dc2.getMinDate());
        assertEquals(dateToCheck, dc3.getMinDate());
        assertEquals(dateToCheck, dc4.getMinDate());
        assertEquals(dateToCheck, dc5.getMinDate());
    }

    private class TempDeviceFactory {

        private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss z");

        public Device get(long id, String imei, User owner, String dateStr) throws ParseException {
            Device device = new Device();
            device.setId(id);
            device.setUniqueId(imei);
            device.setOwner(owner);
            device.setUsers(Collections.singleton(owner));
            device.setMaintenances(Collections.EMPTY_LIST);
            device.setLastUpdate(format.parse(dateStr));

            return device;
        }
    }
}
