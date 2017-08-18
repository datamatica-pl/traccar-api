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
import spark.Session;
import org.junit.*;
import static org.junit.Assert.*;
import org.mockito.Mockito;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.auth.BasicAuthFilter;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.in.NotificationTokenDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.dtos.out.UserDto;
import pl.datamatica.traccar.api.providers.SessionProvider;
import pl.datamatica.traccar.api.responses.*;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserSettings;

public class SessionControllerTest {
    
    private User requestUser;
    private SessionController controller;
    private boolean isTokenValid;
    private Session session;
    private SessionProvider sp;
    
    @Before
    public void testInit() {
        session = Mockito.mock(Session.class);
        sp = Mockito.mock(SessionProvider.class);
        requestUser = new User();
        requestUser.setLogin("test");
        requestUser.setUserSettings(new UserSettings());
        RequestContext rc = Mockito.mock(RequestContext.class);
        Mockito.when(rc.getUser()).thenReturn(requestUser);
        Mockito.when(rc.getSessionProvider()).thenReturn(sp);
        Mockito.when(rc.session()).thenReturn(session);
        controller = new SessionController(rc, token -> isTokenValid);
    }
    
    @Test
    public void getUser() {
        HttpResponse actual = controller.getUser();
        
        assertTrue(actual instanceof OkResponse);
        assertTrue(actual.getContent() instanceof UserDto);
    }
    
    @Test
    public void putNotificationToken_ok() {
        String token = "3";
        isTokenValid = true;
        
        HttpResponse response = controller.putNotificationToken(new NotificationTokenDto(token));
        
        Mockito.verify(sp, Mockito.times(1)).createSession(null, token);
        assertTrue(response instanceof OkResponse);
        assertEquals("", response.getContent());
    }
    
    @Test
    public void putNotificationToken_invalidToken() {
        ErrorDto expectedError = new ErrorDto(MessageKeys.ERR_TOKEN_REJECTED);
        isTokenValid = false;
        
        HttpResponse response = controller.putNotificationToken(new NotificationTokenDto("5"));
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(400, response.getHttpStatus());
        List<ErrorDto> errors = (List<ErrorDto>) response.getContent();
        assertEquals(1, errors.size());
        assertEquals(expectedError, errors.get(0));
    }
    
    @Test
    public void putNotificationToken_noToken() {
        ErrorDto expectedError = new ErrorDto(MessageKeys.ERR_DATA_NOT_PROVIDED);
        
        HttpResponse response = controller.putNotificationToken(null);
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(400, response.getHttpStatus());
        List<ErrorDto> errors = (List<ErrorDto>) response.getContent();
        assertEquals(1, errors.size());
        assertEquals(expectedError, errors.get(0));
    }
    
    @Test
    public void delete() {
        HttpResponse response = controller.delete();
        
        Mockito.verify(session, Mockito.times(1)).invalidate();
        assertTrue(response instanceof OkResponse);
        assertEquals("", response.getContent());
    }
}
