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
import pl.datamatica.traccar.api.dtos.out.DeviceDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.providers.DeviceProvider;
import pl.datamatica.traccar.api.responses.*;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;

public class DevicesControllerTest {
    
    private User user;
    private DeviceProvider dp;
    private DevicesController dc;
    private RequestContext rc;
    private List<Device> devices;
    
    
    @Before
    public void testInit() {
        user = new User();
        rc = Mockito.mock(RequestContext.class);
        dp = Mockito.mock(DeviceProvider.class);
        Mockito.when(rc.getDeviceProvider()).thenReturn(dp);
        Mockito.when(rc.getUser()).thenReturn(user);
        Mockito.when(rc.getModificationDate()).thenReturn(new Date(0));
        dc = new DevicesController(rc);
        devices = IntStream.range(0, 3)
                .mapToObj(i -> {
                    Device device = new Device();
                    device.setUniqueId(i+"");
                    device.setOwner(user);
                    device.setLastUpdate(new Date((i+1)*1000));
                    return device;
                })
                .collect(Collectors.toList());
        user.getDevices().add(devices.get(0));
        
        Mockito.when(dp.getDevice(0)).thenReturn(devices.get(0));
        Mockito.when(dp.isVisible(devices.get(0))).thenReturn(true);
        Mockito.when(dp.getDevice(2)).thenReturn(devices.get(2));
        Mockito.when(dp.isVisible(devices.get(2))).thenReturn(false);
        Mockito.when(dp.getAllAvailableDevices()).thenReturn(devices.stream());
    }
    
    @Test
    public void getAll_emptyList() throws Exception {
        HttpHeader expected = lastModifiedHeader(new Date(1000));
        Mockito.when(dp.getAllAvailableDevices()).thenReturn(Stream.empty());
        
        HttpResponse response = dc.get();
        
        assertTrue(response instanceof OkCachedResponse);
        List<DeviceDto> actual = (List<DeviceDto>)response.getContent();
        assertTrue(actual.isEmpty());
        assertTrue(getHeaderStream(response).anyMatch(h -> h.equals(expected)));
    }
    
    @Test
    public void getAll_emptyListCached() throws Exception {
        HttpHeader expected = lastModifiedHeader(new Date(1000));
        Mockito.when(rc.getModificationDate()).thenReturn(new Date(5000));
        Mockito.when(dp.getAllAvailableDevices()).thenReturn(Stream.empty());
        
        HttpResponse response = dc.get();
        
        assertTrue(response instanceof NotModifiedResponse);
        assertTrue(getHeaderStream(response).anyMatch(h -> h.equals(expected)));
    }
    
    @Test
    public void getAll_expectedOnlyModified() throws Exception {
        HttpHeader expected = lastModifiedHeader(new Date(3000));
        Mockito.when(rc.getModificationDate()).thenReturn(new Date(2000));
        
        HttpResponse response = dc.get();
        
        assertTrue(response instanceof OkResponse);
        assertTrue(getHeaderStream(response).anyMatch(h -> h.equals(expected)));
        List<DeviceDto> actual = (List<DeviceDto>)response.getContent();
        assertEquals(1, actual.size());
    }
    
    @Test
    public void getAll_nonEmptyList() throws Exception {
        HttpResponse response = dc.get();
        
        assertTrue(response instanceof OkCachedResponse);
        assertTrue(response.getContent() instanceof List);
        List<DeviceDto> actual = (List<DeviceDto>)response.getContent();
        assertEquals(3, actual.size());
        for(Object item : actual)
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
        HttpResponse response = dc.get(2);
        assertTrue(response instanceof ErrorResponse);
        assertEquals(403, response.getHttpStatus());
    }
    
    @Test
    public void getOne_notFound() throws Exception {
        HttpResponse response = dc.get(4);
        assertTrue(response instanceof ErrorResponse);
        assertEquals(404, response.getHttpStatus());
    }
    
    @Test
    public void post_validImei()  throws Exception {
        final String uniqueId = "5";
        final long id = 0;
        
        Device expectedContent = new Device();
        expectedContent.setUniqueId(uniqueId);
        Mockito.when(dp.createDevice(uniqueId)).thenReturn(expectedContent);
        AddDeviceDto deviceDto = new AddDeviceDto(uniqueId);
        
        HttpResponse response = dc.post(deviceDto);
        
        HttpHeader expectedHdr = new HttpHeader("Location", "devices/"+id);
        assertTrue(response instanceof CreatedResponse);
        Stream<HttpHeader> headers = getHeaderStream(response);
        assertTrue(headers.anyMatch(h -> h.equals(expectedHdr)));
        assertEquals(expectedContent, response.getContent());
    }
    
    @Test
    public void post_noImei() throws Exception {
        AddDeviceDto deviceDto = new AddDeviceDto();
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
        
        HttpResponse response = dc.post(deviceDto);
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(400, response.getHttpStatus());
        List<ErrorDto> errors = (List<ErrorDto>)response.getContent();
        assertEquals(1, errors.size());
        assertEquals(expectedError, errors.get(0));
    }
}
