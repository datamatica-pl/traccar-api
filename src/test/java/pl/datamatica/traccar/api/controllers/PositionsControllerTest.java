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

import static org.junit.Assert.*;
import org.junit.*;
import org.mockito.Mockito;
import pl.datamatica.traccar.api.dtos.out.PositionDto;
import pl.datamatica.traccar.api.providers.PositionProvider;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.api.responses.*;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Position;

public class PositionsControllerTest {
    private PositionProvider provider;
    private PositionsController controller;
    
    @Before
    public void testInit() {
        provider = Mockito.mock(PositionProvider.class);
        RequestContext rc = Mockito.mock(RequestContext.class);
        Mockito.when(rc.getPositionProvider()).thenReturn(provider);
        
        controller = new PositionsController(rc);
    }
    
    @Test
    public void get_ok() throws ProviderException {
        Position p = new Position();
        p.setLatitude(48.);
        p.setLongitude(22.);
        p.setValid(true);
        p.setDevice(new Device());
        Mockito.when(provider.get(0)).thenReturn(p);
        
        HttpResponse result = controller.get(0);
        
        assertTrue(result instanceof OkResponse);
        assertTrue(result.getContent() instanceof PositionDto);
    }
    
    @Test
    public void get_notFound() throws Exception {
        Mockito.when(provider.get(1)).thenThrow(new ProviderException(Type.NOT_FOUND));
        
        HttpResponse response = controller.get(1);
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(404, response.getHttpStatus());
    }
    
    @Test
    public void get_forbidden() throws Exception {
        Mockito.when(provider.get(2)).thenThrow(new ProviderException(Type.ACCESS_DENIED));
        
        HttpResponse response = controller.get(2);
        
        assertTrue(response instanceof ErrorResponse);
        assertEquals(403, response.getHttpStatus());
    }
}
