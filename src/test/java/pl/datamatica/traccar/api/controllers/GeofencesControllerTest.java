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
import java.util.stream.Stream;
import org.junit.*;
import static org.junit.Assert.*;
import org.mockito.Mockito;
import pl.datamatica.traccar.api.dtos.in.AddGeoFenceDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.dtos.out.GeoFenceDto;
import pl.datamatica.traccar.api.dtos.out.PointDto;
import pl.datamatica.traccar.api.providers.GeoFenceProvider;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.api.responses.CreatedResponse;
import pl.datamatica.traccar.api.responses.ErrorResponse;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.api.responses.OkResponse;
import pl.datamatica.traccar.model.GeoFence;

public class GeofencesControllerTest {
    GeoFenceProvider provider;
    GeofencesController controller;
    
    
    @Before
    public void testInit() {
        provider = Mockito.mock(GeoFenceProvider.class);
        RequestContext rc = Mockito.mock(RequestContext.class);
        Mockito.when(rc.getGeoFencesProvider()).thenReturn(provider);
        Mockito.when(rc.getModificationDate()).thenReturn(new Date(0));
        controller = new GeofencesController(rc);
    }
    
    @Test
    public void getAll_ok() {
        Mockito.when(provider.getAllAvailableGeoFences()).thenReturn(Stream.of(new GeoFence()));
        
        HttpResponse response = controller.get();
        
        assertTrue(response instanceof OkResponse);
        assertTrue(response.getContent() instanceof List);
        List result = (List)response.getContent();
        assertTrue(result.stream().allMatch(item -> item instanceof GeoFenceDto));
    }
    
    @Test
    public void getOne_ok() throws ProviderException {
        Mockito.when(provider.getGeoFence(1)).thenReturn(new GeoFence());
        
        HttpResponse response = controller.get(1);
        
        assertTrue(response instanceof OkResponse);
        assertTrue(response.getContent() instanceof GeoFenceDto);
    }
    
    @Test
    public void getOne_notFound() throws Exception {
        Mockito.when(provider.getGeoFence(2)).thenThrow(new ProviderException(Type.NOT_FOUND));
        
        HttpResponse response = controller.get(2);
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(404, response.getHttpStatus());
    }
    
    @Test
    public void getOne_forbidden() throws Exception {
        Mockito.when(provider.getGeoFence(3)).thenThrow(new ProviderException(Type.ACCESS_DENIED));
        
        HttpResponse response = controller.get(3);
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(403, response.getHttpStatus());
    }
    
    @Test
    public void post_ok() throws Exception {
        AddGeoFenceDto geofenceDto = new AddGeoFenceDto.Builder()
                .allDevices(true)
                .color("EEAA88")
                .geofenceName("mój geopłot")
                .description("Najważniejszy z moich geopłotów")
                .points(Collections.singletonList(new PointDto(51, 18)))
                .radius(100)
                .type("CIRCLE")
                .deviceIds(new long[0])
                .build();
        Mockito.when(provider.createGeoFence(geofenceDto)).thenReturn(new GeoFence());
        
        HttpResponse response = controller.post(geofenceDto);
        
        assertTrue(response instanceof CreatedResponse);
        assertTrue(response.getContent() instanceof GeoFenceDto);
    }
    
    @Test
    public void post_invalidData() throws Exception {
        //invalid color format, all devices not provided
        AddGeoFenceDto invalidGeofence = new AddGeoFenceDto.Builder()
                .color("03AbCf")
                .geofenceName("test")
                .points(Collections.singletonList(new PointDto(21, 52)))
                .radius(50)
                .type("CIRCLE")
                .deviceIds(new long[0])
                .build();
        Mockito.when(provider.createGeoFence(invalidGeofence)).thenReturn(new GeoFence());
        
        HttpResponse response = controller.post(invalidGeofence);
        
        assertTrue(response instanceof ErrorResponse);
        List<ErrorDto> errors = (List<ErrorDto>) response.getContent();
        assertEquals(2, errors.size());
    }
    
    @Test
    public void put_ok() throws ProviderException {
        AddGeoFenceDto geofenceDto = new AddGeoFenceDto.Builder()
                .allDevices(false)
                .color("FF5599")
                .geofenceName("mój geopłot")
                .description("Najważniejszy z moich geopłotów")
                .points(Collections.singletonList(new PointDto(51, 18)))
                .radius(100)
                .type("CIRCLE")
                .deviceIds(new long[0])
                .build();
        
        HttpResponse response = controller.put(5, geofenceDto);
        
        assertTrue(response instanceof OkResponse);
        assertEquals("", response.getContent());
    }
    
    @Test
    public void put_invalidData() throws ProviderException {
        //invalid color format, all devices not provided, not device ids
        AddGeoFenceDto invalidGeofence = new AddGeoFenceDto.Builder()
                .color("03AbCf")
                .geofenceName("test")
                .points(Collections.singletonList(new PointDto(21, 52)))
                .radius(50)
                .type("CIRCLE")
                .build();
        
        HttpResponse response = controller.put(5, invalidGeofence);
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(400, response.getHttpStatus());
        List<ErrorDto> errors = (List<ErrorDto>)response.getContent();
        assertEquals(3, errors.size());
    }
}
