/*
 *  Copyright (C) 2016  Datamatica (dev@datamatica.pl)
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static pl.datamatica.traccar.api.controllers.ControllerTest.*;
import pl.datamatica.traccar.api.controllers.DevicesController.PositionsQueryParams;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.in.AddDeviceDto;
import pl.datamatica.traccar.api.dtos.in.EditDeviceDto;
import pl.datamatica.traccar.api.dtos.out.DeviceDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.dtos.out.ListDto;
import pl.datamatica.traccar.api.dtos.out.PositionDto;
import pl.datamatica.traccar.api.providers.DeviceProvider;
import pl.datamatica.traccar.api.providers.PositionProvider;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.api.providers.UserProvider;
import pl.datamatica.traccar.api.responses.*;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.User;

public class DevicesControllerTest {
    
    private User user;
    private DeviceProvider dp;
    private PositionProvider pp;
    private UserProvider up;
    private DevicesController dc;
    private RequestContext rc;
    private List<Device> devices;
    
    
    @Before
    public void testInit() throws ProviderException {
        user = new User();
        rc = Mockito.mock(RequestContext.class);
        dp = Mockito.mock(DeviceProvider.class);
        pp = Mockito.mock(PositionProvider.class);
        up = Mockito.mock(UserProvider.class);
        Mockito.when(rc.getDeviceProvider()).thenReturn(dp);
        Mockito.when(rc.getPositionProvider()).thenReturn(pp);
        Mockito.when(rc.getUserProvider()).thenReturn(up);
        Mockito.when(rc.getUser()).thenReturn(user);
        Mockito.when(up.getAllAvailableUsers()).thenReturn(Stream.<User>empty());
        dc = new DevicesController(rc);
        devices = IntStream.range(0, 3)
                .mapToObj(i -> {
                    Device device = new Device();
                    device.setUniqueId(i+"");
                    device.setOwner(user);
                    device.setUsers(Collections.singleton(user));
                    device.setMaintenances(Collections.EMPTY_LIST);
                    device.setLastUpdate(new Date((i+1)*1000));
                    return device;
                })
                .collect(Collectors.toList());
        user.getDevices().add(devices.get(0));
        
        Mockito.when(dp.getDevice(0)).thenReturn(devices.get(0));
        Mockito.when(dp.getAllAvailableDevices()).thenReturn(devices.stream());
    }
    
    @Test
    public void getAll_emptyList() throws Exception {
        Mockito.when(dp.getAllAvailableDevices()).thenReturn(Stream.<Device>empty());
        
        HttpResponse response = dc.get();
        
        assertTrue(response instanceof OkCachedResponse);
        ListDto<DeviceDto> actual = (ListDto<DeviceDto>)response.getContent();
        assertTrue(actual.getChanged().isEmpty());
        assertTrue(actual.getIds().length == 0);
    }
    
    @Test
    public void getAll_emptyListCached() throws Exception {
        Mockito.when(rc.getModificationDate()).thenReturn(new Date(5000));
        Mockito.when(dp.getAllAvailableDevices()).thenReturn(Stream.empty());
        
        HttpResponse response = dc.get();
        
        assertTrue(response instanceof OkCachedResponse);
    }
    
    @Test
    public void getAll_expectedOnlyModified() throws Exception {
        HttpHeader expected = lastModifiedHeader(new Date(3000));
        Mockito.when(rc.getModificationDate()).thenReturn(new Date(2000));
        
        HttpResponse response = dc.get();
        
        assertTrue(response instanceof OkResponse);
        assertTrue(getHeaderStream(response).anyMatch(h -> h.equals(expected)));
        ListDto<DeviceDto> actual = (ListDto<DeviceDto>)response.getContent();
        assertEquals(3, actual.getIds().length);
        assertEquals(1, actual.getChanged().size());
    }
    
    @Test
    public void getAll_nonEmptyList() throws Exception {
        HttpResponse response = dc.get();
        
        assertTrue(response instanceof OkCachedResponse);
        assertTrue(response.getContent() instanceof ListDto);
        ListDto<DeviceDto> actual = (ListDto<DeviceDto>)response.getContent();
        assertEquals(3, actual.getChanged().size());
        for(Object item : actual.getChanged())
            assertTrue(item instanceof DeviceDto);
    }
    
    @Test
    public void getOne_ok() throws Exception {
        HttpHeader expected = lastModifiedHeader(new Date(1000));
        
        HttpResponse response = dc.get(0);
        
        assertTrue(response instanceof OkCachedResponse);
        assertTrue(response.getContent() instanceof DeviceDto);
        Stream<HttpHeader> headers= getHeaderStream(response);
        assertTrue(headers.anyMatch(h-> h.equals(expected)));
    }
    
    @Test
    public void getOne_cached() throws Exception {
        HttpHeader expected = lastModifiedHeader(new Date(1000));
        Mockito.when(rc.getModificationDate()).thenReturn(new Date(1000));
        
        HttpResponse response = dc.get(0);
        
        assertTrue(response instanceof NotModifiedResponse);   
        assertTrue(getHeaderStream(response).anyMatch(h -> h.equals(expected)));
    }
    
    @Test
    public void getOne_forbidden() throws Exception {
        Mockito.when(dp.getDevice(2)).thenThrow(new ProviderException(Type.ACCESS_DENIED));
        HttpResponse response = dc.get(2);
        assertTrue(response instanceof ErrorResponse);
        assertEquals(403, response.getHttpStatus());
    }
    
    @Test
    public void getOne_notFound() throws Exception {
        Mockito.when(dp.getDevice(4)).thenThrow(new ProviderException(Type.NOT_FOUND));
        HttpResponse response = dc.get(4);
        assertTrue(response instanceof ErrorResponse);
        assertEquals(404, response.getHttpStatus());
    }
    
    @Test
    public void getPositions_ok() throws Exception {
        Position position = new Position();
        position.setLatitude(1.);
        position.setLongitude(1.);
        position.setTime(new Date());
        position.setValid(true);
        position.setDevice(devices.get(0));
        position.setServerTime(new Date());
        Mockito.when(pp.getAllAvailablePositions(Mockito.eq(devices.get(0)), 
                Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt()))
                .thenReturn(Stream.of(position));
        
        HttpResponse response = dc.getPositions(0, new HashMap<>());
        
        assertTrue(response instanceof OkCachedResponse);
        assertTrue(response.getContent() instanceof ListDto);
        ListDto<PositionDto> result = (ListDto<PositionDto>)response.getContent();
        assertEquals(1, result.getChanged().size());
        assertNull(result.getIds());
    }
    
    @Test
    public void getPositions_forbidden() throws Exception {
        Mockito.when(dp.getDevice(3)).thenThrow(new ProviderException(Type.ACCESS_DENIED));
        
        HttpResponse response = dc.getPositions(3, new HashMap<>());
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(403, response.getHttpStatus());
    }
    
    @Test
    public void getPositions_notFound() throws Exception {
        Mockito.when(dp.getDevice(5)).thenThrow(new ProviderException(Type.NOT_FOUND));
        
        HttpResponse response = dc.getPositions(5, new HashMap<>());
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(404, response.getHttpStatus());
    }
    
    @Test
    public void post_validImei()  throws Exception {
        final String uniqueId = "5";
        final long id = 0;
        
        Device expectedContent = new Device();
        expectedContent.setOwner(user);
        expectedContent.setUsers(Collections.singleton(user));
        expectedContent.setMaintenances(Collections.EMPTY_LIST);
        expectedContent.setUniqueId(uniqueId);
        Mockito.when(dp.createDevice(uniqueId)).thenReturn(expectedContent);
        AddDeviceDto deviceDto = new AddDeviceDto(uniqueId);
        
        HttpResponse response = dc.post(deviceDto);
        
        HttpHeader expectedHdr = new HttpHeader("Location", "devices/"+id);
        assertTrue(response instanceof CreatedResponse);
        Stream<HttpHeader> headers = getHeaderStream(response);
        assertTrue(headers.anyMatch(h -> h.equals(expectedHdr)));
        assertTrue(response.getContent() instanceof DeviceDto);
    }
    
    @Test
    public void post_noImei() throws Exception {
        AddDeviceDto deviceDto = new AddDeviceDto(null);
        ErrorDto expectedError = new ErrorDto(MessageKeys.ERR_DATA_NOT_PROVIDED);
        
        HttpResponse response = dc.post(deviceDto);
             
        assertTrue(response instanceof ErrorResponse);
        assertEquals(400, response.getHttpStatus());
        List<ErrorDto> errors = (List<ErrorDto>)response.getContent();
        assertEquals(1, errors.size());
        assertEquals(expectedError, errors.get(0));
    }
    
    @Test
    public void post_invalidImei() throws Exception {
        String imei = "8";
        AddDeviceDto deviceDto = new AddDeviceDto(imei);
        ErrorDto expectedError = new ErrorDto(MessageKeys.ERR_INVALID_IMEI);
        Mockito.when(dp.createDevice("8")).thenThrow(new ProviderException(Type.INVALID_IMEI));
        
        HttpResponse response = dc.post(deviceDto);
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(400, response.getHttpStatus());
        List<ErrorDto> errors = (List<ErrorDto>)response.getContent();
        assertEquals(1, errors.size());
        assertEquals(expectedError, errors.get(0));
    }
    
    @Test
    public void put_ok() throws Exception {
        EditDeviceDto deviceDto = new EditDeviceDto.Builder()
                .color("00FF00")
                .deviceModelId(-1)
                .iconId(1L)
                .deviceName("test")
                .build();
        
        dc.put(5, deviceDto);
        
        Mockito.verify(dp, Mockito.times(1)).updateDevice(5, deviceDto);
    }
    
    @Test
    public void put_invalidData() throws ProviderException {
        EditDeviceDto deviceDto = new EditDeviceDto.Builder()
                .color("")
                .deviceModelId(5)
                .deviceName("test")
                .build();
        
        HttpResponse response = dc.put(5, deviceDto);
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(400, response.getHttpStatus());
        List<ErrorDto> errors = (List<ErrorDto>)response.getContent();
        assertEquals(2, errors.size());
    }
    
    @Test
    public void delete_ok() throws Exception {
        HttpResponse response = dc.delete(3);
        
        assertTrue(response instanceof OkResponse);
        assertEquals("", response.getContent());
    }
    
    @Test
    public void delete_notFound() throws Exception {
        Mockito.doThrow(new ProviderException(Type.NOT_FOUND)).when(dp).delete(7);
        
        HttpResponse response = dc.delete(7);
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(404, response.getHttpStatus());
    }
    
    @Test
    public void delete_accessDenied() throws Exception {
        Mockito.doThrow(new ProviderException(Type.ACCESS_DENIED)).when(dp).delete(7);
        
        HttpResponse response = dc.delete(7);
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(403, response.getHttpStatus());
    }
    
    // POSITIONS FILTER TESTS
    
    @Test
    public void parseQuery_invalidDateFormat() {
        Map<String, String[]> query = new HashMap<>();
        query.put("fromDate", new String[] { "almostDate" });
        query.put("toDate", new String[] { "almostDate2" });
        
        PositionsQueryParams res = dc.parsePositionsQuery(query);
    
        assertNull(res.minDate);
        assertNull(res.maxDate);
        assertEquals(1, res.errors.size());
        assertEquals(MessageKeys.ERR_POSITIONS_QUERY_DATE_WRONG_FORMAT, res.errors.get(0).getMessageKey());
    }
    
    @Test
    public void parseQuery_dateNotProvided() {
        Map<String, String[]> query = new HashMap<>();
        query.put("fromDate", new String[] { });
        query.put("toDate", new String[] { "DATA" });
        
        PositionsQueryParams res = dc.parsePositionsQuery(query);
    
        assertNull(res.minDate);
        assertNull(res.maxDate);
        assertEquals(1, res.errors.size());
        assertEquals(MessageKeys.ERR_POSITIONS_QUERY_DATE_VALUE_NOT_PROVIDED, res.errors.get(0).getMessageKey());
    }
    
    @Test
    public void parseQuery_invalidMinDistanceFormat() {
        Map<String, String[]> query = new HashMap<>();
        query.put("minDistance", new String[] { "siedem" });
        
        PositionsQueryParams res = dc.parsePositionsQuery(query);
    
        assertEquals(0, res.minDistance.intValue());
        assertEquals(1, res.errors.size());
        assertEquals(MessageKeys.ERR_POSITIONS_QUERY_MINDISTANCE_WRONG_FORMAT, res.errors.get(0).getMessageKey());
    }
    
    @Test
    public void parseQuery_invalidSpeedCompFormat() {
        Map<String, String[]> query = new HashMap<>();
        query.put("speedComp", new String[] { "mniejsze" });
        query.put("speedValue", new String[] { "7" });
        
        PositionsQueryParams res = dc.parsePositionsQuery(query);
    
        assertNull(res.speedValue);
        assertEquals(1, res.errors.size());
        assertEquals(MessageKeys.ERR_POSITIONS_QUERY_SPEED_WRONG_FORMAT, res.errors.get(0).getMessageKey());
    }
    
    @Test
    public void parseQuery_invalidSpeedValueFormat() {
        Map<String, String[]> query = new HashMap<>();
        query.put("speedComp", new String[] { "lt" });
        query.put("speedValue", new String[] { "siedem" });
        
        PositionsQueryParams res = dc.parsePositionsQuery(query);
    
        assertNull(res.speedValue);
        assertEquals(1, res.errors.size());
        assertEquals(MessageKeys.ERR_POSITIONS_QUERY_SPEED_WRONG_FORMAT, res.errors.get(0).getMessageKey());
    }
    
    @Test
    public void parseQuery_multipleErrors() {
        Map<String, String[]> query = new HashMap<>();
        query.put("fromDate", new String[] { });
        query.put("toDate", new String[] { "DATA" });
        query.put("minDistance", new String[] { });
        query.put("speedComp", new String[] { "mniejsze" });
        query.put("speedValue", new String[] { "7" });
        
        PositionsQueryParams res = dc.parsePositionsQuery(query);
    
        assertEquals(3, res.errors.size());
        assertEquals(MessageKeys.ERR_POSITIONS_QUERY_DATE_VALUE_NOT_PROVIDED, res.errors.get(0).getMessageKey());
        assertEquals(MessageKeys.ERR_POSITIONS_QUERY_MINDISTANCE_VALUE_NOT_PROVIDED, res.errors.get(1).getMessageKey());
        assertEquals(MessageKeys.ERR_POSITIONS_QUERY_SPEED_WRONG_FORMAT, res.errors.get(2).getMessageKey());
    }
    
    @Test
    public void parseQuery_success() {
        Map<String, String[]> query = new HashMap<>();
        query.put("fromDate", new String[] { "2017-08-02T09:32:25Z"});
        query.put("toDate", new String[] { "2017-08-02T09:35:25Z" });
        query.put("minDistance", new String[] { "2" });
        query.put("speedComp", new String[] { "gte" });
        query.put("speedValue", new String[] { "15" });
        query.put("hideZero", new String[] { });
        query.put("hideInvalid", new String[] { });
        query.put("hideDup", new String[] { });
        query.put("all", new String[] { });
        
        PositionsQueryParams res = dc.parsePositionsQuery(query);
    
        assertNotNull(res);
        assertEquals(0, res.errors.size());
        assertNotNull(res.minDate);
        assertNotNull(res.maxDate);
        assertTrue(res.minDate.before(res.maxDate));
        assertTrue(res.hideZero);
        assertTrue(res.hideInvalid);
        assertTrue(res.hideDuplicates);
        assertTrue(res.getAll);
        assertEquals(DevicesController.PositionSpeedOperator.GREATEREQUAL, res.speedComp);
        assertEquals(15, res.speedValue.intValue());
        assertEquals(2, res.minDistance.intValue());
    }
    
    @Test
    public void filterPositions_hideZero() {
        List<Position> list = getFilterTestPositionsList();
        PositionsQueryParams qp = dc.new PositionsQueryParams();
        qp.hideZero = true;
        
        List<Position> res = dc.filterPositions(list, qp);
        
        assertNotNull(res);
        assertEquals(list.size() - 2, res.size());
        for (Position re : res) {
            assertTrue(re.getLatitude() != 0 || re.getLongitude() != 0);
        }
    }
    
    @Test
    public void filterPositions_hideInvalid() {
        List<Position> list = getFilterTestPositionsList();
        PositionsQueryParams qp = dc.new PositionsQueryParams();
        qp.hideInvalid = true;
        
        List<Position> res = dc.filterPositions(list, qp);
        
        assertNotNull(res);
        assertEquals(list.size() - 2, res.size());
        for (Position re : res) {
            assertTrue(re.getValid());
        }
    }
        
    @Test
    public void filterPositions_hideDuplicates() {
        List<Position> list = getFilterTestPositionsList();
        PositionsQueryParams qp = dc.new PositionsQueryParams();
        qp.hideDuplicates = true;
        
        List<Position> res = dc.filterPositions(list, qp);
        
        assertNotNull(res);
        assertEquals(list.size() - 3, res.size());
    }    
      
    @Test
    public void filterPositions_minDistance() {
    List<Position> list = getFilterTestPositionsList();
        PositionsQueryParams qp = dc.new PositionsQueryParams();
        qp.minDistance = 100;
        
        List<Position> res = dc.filterPositions(list, qp);
        
        assertNotNull(res);
        assertEquals(list.size() - 6, res.size());
    }   
    
    @Test
    public void filterPositions_speedEqual() {
    List<Position> list = getFilterTestPositionsList();
        PositionsQueryParams qp = dc.new PositionsQueryParams();
        qp.speedComp = DevicesController.PositionSpeedOperator.EQUAL;
        qp.speedValue = 10;
        
        List<Position> res = dc.filterPositions(list, qp);
        
        assertNotNull(res);
        assertEquals(4, res.size());
    }
    
    @Test
    public void filterPositions_speedLess() {
        List<Position> list = getFilterTestPositionsList();
        PositionsQueryParams qp = dc.new PositionsQueryParams();
        qp.speedComp = DevicesController.PositionSpeedOperator.LESS;
        qp.speedValue = 19;
        
        List<Position> res = dc.filterPositions(list, qp);
        
        assertNotNull(res);
        assertEquals(6, res.size());
    }
    
    @Test
    public void filterPositions_combined() {
        List<Position> list = getFilterTestPositionsList();
        PositionsQueryParams qp = dc.new PositionsQueryParams();
        qp.speedComp = DevicesController.PositionSpeedOperator.LESSEQUAL;
        qp.speedValue = 25;
        qp.minDistance = 100;
        qp.hideInvalid = true;
        qp.hideZero = true;
        
        List<Position> res = dc.filterPositions(list, qp);
        
        assertNotNull(res);
        assertEquals(2, res.size());
    }
    
    //
    private List<Position> getFilterTestPositionsList() {
        final long MINUTE = 60000;//millisecs
        
        Calendar date = Calendar.getInstance();
        long baseTime = date.getTimeInMillis() - 60 * MINUTE;

        List<Position> positions = new ArrayList<>();
        positions.add(preparePosition(52., 19., 10., new Date(baseTime)));
        positions.add(preparePosition(52., 19., 10., new Date(baseTime))); //duplicate
        positions.add(preparePosition(52., 20., 20., new Date(baseTime + MINUTE))); 
        positions.add(preparePosition(52., 20., 10., new Date(baseTime + 2 * MINUTE))); //same location
        positions.add(preparePosition(52., 20.0001, 20., new Date(baseTime + 3 * MINUTE))); //distance < 100m from previous
        positions.add(preparePosition(52., 20.0002, 30., new Date(baseTime + 3 * MINUTE))); // time duplicate
        positions.add(preparePosition(52., 20., null, new Date(baseTime + 4 * MINUTE))); //null speed
        positions.add(preparePosition(0., 0., 30., new Date(baseTime + 5 * MINUTE))); //zero location
        positions.add(preparePosition(52., 12., 30., new Date(baseTime + 6 * MINUTE))); //invalid
        positions.get(positions.size() - 1).setValid(false);
        positions.add(preparePosition(0., 0., 25., new Date(baseTime + 7 * MINUTE))); //zero location
        positions.add(preparePosition(53., 12., 10., new Date(baseTime + 8 * MINUTE))); //invalid
        positions.get(positions.size() - 1).setValid(false);
        positions.add(preparePosition(52., 20., 5., new Date(baseTime + 10 * MINUTE))); //null speed
        positions.add(preparePosition(52., 20., 5., new Date(baseTime + 10 * MINUTE))); //duplicate
        positions.add(preparePosition(0., 20., 50., new Date(baseTime + 11 * MINUTE))); //duplicate

        return positions;
    }
    
    private Position preparePosition(double lat, double lon, Double speed, Date date) {
        Position pos = new Position();
        pos.setLatitude(lat);
        pos.setLongitude(lon);
        pos.setSpeed(speed);
        pos.setTime(date);
        pos.setServerTime(date);
        pos.setValid(true);
        return pos;
    }
    
}
