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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static pl.datamatica.traccar.api.controllers.ControllerTest.*;
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
        Mockito.when(pp.getAllAvailablePositions(Mockito.eq(devices.get(0)), 
                Mockito.any(Date.class), Mockito.anyInt()))
                .thenReturn(Stream.of(position));
        
        HttpResponse response = dc.getPositions(0);
        
        assertTrue(response instanceof OkCachedResponse);
        assertTrue(response.getContent() instanceof ListDto);
        ListDto<PositionDto> result = (ListDto<PositionDto>)response.getContent();
        assertEquals(1, result.getChanged().size());
        assertNull(result.getIds());
    }
    
    @Test
    public void getPositions_forbidden() throws Exception {
        Mockito.when(dp.getDevice(3)).thenThrow(new ProviderException(Type.ACCESS_DENIED));
        
        HttpResponse response = dc.getPositions(3);
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(403, response.getHttpStatus());
    }
    
    @Test
    public void getPositions_notFound() throws Exception {
        Mockito.when(dp.getDevice(5)).thenThrow(new ProviderException(Type.NOT_FOUND));
        
        HttpResponse response = dc.getPositions(5);
        
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
        ErrorDto expectedError = new ErrorDto(MessageKeys.ERR_IMEI_NOT_PROVIDED);
        
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
}
