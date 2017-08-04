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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.*;
import static org.junit.Assert.*;
import org.mockito.Mockito;
import pl.datamatica.traccar.api.dtos.in.RegisterUserDto;
import pl.datamatica.traccar.api.dtos.out.UserDto;
import pl.datamatica.traccar.api.providers.DeviceProvider;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.api.providers.UserProvider;
import pl.datamatica.traccar.api.responses.CreatedResponse;
import pl.datamatica.traccar.api.responses.ErrorResponse;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.api.responses.OkResponse;
import pl.datamatica.traccar.model.User;

public class UsersControllerTest {
    
    private UsersController controller;
    private DeviceProvider devices;
    private UserProvider provider;
    private RequestContext rc;
    private User user;
    private List<User> users;
    private RegisterUserDto userDto;
    
    @Before
    public void testInit() {
        user = new User();
        user.setLogin("0");
        rc = Mockito.mock(RequestContext.class);
        provider = Mockito.mock(UserProvider.class);
        devices = Mockito.mock(DeviceProvider.class);
        Mockito.when(rc.getUser()).thenReturn(user);
        Mockito.when(rc.getUserProvider()).thenReturn(provider);
        Mockito.when(rc.getDeviceProvider()).thenReturn(devices);
        Mockito.when(rc.getApiRoot()).thenReturn("");
        controller = new UsersController(rc);
        users = IntStream.range(1, 8)
                .mapToObj(i -> {
                    User u = new User();
                    u.setLogin(i+"");
                    return u;
                })
                .collect(Collectors.toList());
        users.add(user);
        userDto = new RegisterUserDto("test@test.pl", "test", true, "123456");
    }
    
    
    @Test
    public void getAll_ok() throws Exception {
        Mockito.when(provider.getAllAvailableUsers()).thenReturn(users.stream());
        
        HttpResponse response = controller.get();
        
        assertTrue(response instanceof OkResponse);   
        assertTrue(response.getContent() instanceof List);
        List<UserDto> actual = (List<UserDto>)response.getContent();
        assertEquals(users.size(), actual.size());
        for(int i = 0;i < actual.size(); ++i)
            assertEquals(users.get(i).getLogin(), actual.get(i).getLogin());
    }   
    
    @Test
    public void getOne_ok() throws Exception {
        Mockito.when(provider.getUser(1)).thenReturn(users.get(0));
        
        HttpResponse response = controller.get(1);
        
        assertTrue(response instanceof OkResponse);
        assertTrue(response.getContent() instanceof UserDto);
    }
    
    @Test
    public void getOne_forbidden() throws Exception {
        Mockito.when(provider.getUser(1)).thenThrow(new ProviderException(Type.ACCESS_DENIED));
        
        HttpResponse response = controller.get(1);
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(403, response.getHttpStatus());
    }
    
    @Test
    public void getOne_notFound() throws Exception {
        Mockito.when(provider.getUser(1)).thenThrow(new ProviderException(Type.NOT_FOUND));
        HttpResponse response = controller.get(1);
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(404, response.getHttpStatus());
    }
    
    @Test
    public void post_ok() throws ProviderException {
        Mockito.when(provider.registerUser(userDto.getEmail(), userDto.getPassword(), userDto.isCheckMarketing()))
                .thenReturn(new User());
        
        HttpResponse response = controller.post(userDto);
        
        Mockito.verify(devices, Mockito.times(1)).createDevice(userDto.getImei());
        assertTrue(response instanceof CreatedResponse);
        assertEquals("", response.getContent());
    }
    
    @Test
    public void post_noData() throws ProviderException {
        HttpResponse response = controller.post(null);
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(400, response.getHttpStatus());
    }
    
    @Test
    public void post_conflict() throws ProviderException {
        Mockito.when(provider.registerUser(userDto.getEmail(), userDto.getPassword(), userDto.isCheckMarketing()))
                .thenThrow(new ProviderException(Type.USER_ALREADY_EXISTS));

        HttpResponse response = controller.post(userDto);

        assertTrue(response instanceof ErrorResponse);
        assertEquals(409, response.getHttpStatus());
    }
    
    @Test
    public void post_invalidImei() throws ProviderException {
        Mockito.when(devices.createDevice(userDto.getImei()))
                .thenThrow(new ProviderException(Type.INVALID_IMEI));
        
        HttpResponse response = controller.post(userDto);
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(400, response.getHttpStatus());
    }
}
