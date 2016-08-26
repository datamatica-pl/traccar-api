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
    private UserProvider provider;
    private RequestContext rc;
    private User user;
    private List<User> users;
    
    @Before
    public void testInit() {
        user = new User();
        user.setLogin("0");
        rc = Mockito.mock(RequestContext.class);
        provider = Mockito.mock(UserProvider.class);
        Mockito.when(rc.getUser()).thenReturn(user);
        Mockito.when(rc.getUserProvider()).thenReturn(provider);
        controller = new UsersController(rc);
        users = IntStream.range(1, 8)
                .mapToObj(i -> {
                    User u = new User();
                    u.setLogin(i+"");
                    return u;
                })
                .collect(Collectors.toList());
        users.add(user);
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
        String email="test@test.pl";
        String password = "test2015";
        boolean marketing = true;
        RegisterUserDto userDto = new RegisterUserDto(email, password, marketing);
        Mockito.when(provider.createUser(email, password, marketing)).thenReturn(new User());
        
        HttpResponse response = controller.post(userDto);
        
        assertTrue(response instanceof CreatedResponse);
        assertEquals("", response.getContent());
    }
}
