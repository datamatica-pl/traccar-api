///*
// *  Copyright (C) 2016  Datamatica (dev@datamatica.pl)
// * 
// *  This program is free software: you can redistribute it and/or modify
// *  it under the terms of the GNU Affero General Public License as published
// *  by the Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// * 
// *  This program is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *  GNU Affero General Public License for more details.
// * 
// *  You should have received a copy of the GNU Affero General Public License
// *  along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
//package pl.datamatica.traccar.api.controllers;
//
//import java.util.List;
//import java.util.stream.Stream;
//import static org.junit.Assert.*;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Mockito;
//import pl.datamatica.traccar.api.providers.DeviceProvider;
//import pl.datamatica.traccar.api.responses.IHttpResponse;
//import pl.datamatica.traccar.model.Device;
//import pl.datamatica.traccar.model.User;
//
//public class DevicesControllerTest {
//    
//    User user;
//    DeviceProvider dp;
//    DevicesController dc;
//    
//    
//    @Before
//    public void testInit() {
//        user = new User();
//        dp = Mockito.mock(DeviceProvider.class);
//        dc = new DevicesController(new RequestContext(user), rc -> dp);
//    }
//    
//    @Test
//    public void getAll_emptyList() throws Exception {
//        Mockito.when(dp.getAllAvailableDevices(user)).thenReturn(Stream.empty());
//        IHttpResponse response = dc.get();
//        List<Devices> devices = response.write();
//        assertEquals(true, devices.isEmpty());
//    }
//}
