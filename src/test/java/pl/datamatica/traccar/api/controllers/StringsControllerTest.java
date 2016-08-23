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

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.junit.Assert.*;
import org.junit.*;
import org.mockito.Mockito;
import pl.datamatica.traccar.api.dtos.out.FileDto;
import pl.datamatica.traccar.api.providers.FileProvider;
import pl.datamatica.traccar.api.responses.*;
import pl.datamatica.traccar.model.User;
import static pl.datamatica.traccar.api.controllers.ControllerTest.*;


public class StringsControllerTest {
    
    private User user;
    private RequestContext rc;
    private FileProvider fp;
    private StringsController controller;
    private List<FileDto> files;
    
    @Before
    public void testInit() throws Exception {
        user = new User();
        rc = Mockito.mock(RequestContext.class);
        fp = Mockito.mock(FileProvider.class);
        Mockito.when(rc.getModificationDate()).thenReturn(new Date(0));
        Mockito.when(rc.getFileProvider()).thenReturn(fp);
        
        files = IntStream.range(1, 5)
                .mapToObj(i -> new FileDto(i+"", i*1000))
                .collect(Collectors.toList());
        Mockito.when(fp.getAllFiles()).thenReturn(files.stream());
        Mockito.when(fp.getListLastModified()).thenReturn(new Date(2000));
        
        controller = new StringsController(rc);
    }
    
    @Test
    public void getAll_expectedWholeList() throws Exception {
        Mockito.when(rc.getModificationDate()).thenReturn(new Date(3000));
        HttpHeader expected = lastModifiedHeader(new Date(4000));
        
        HttpResponse response = controller.get();
        
        assertTrue(response instanceof OkResponse);
        assertTrue(response.getContent() instanceof List);
        List<FileDto> actual = (List<FileDto>) response.getContent();
        assertEquals(files.size(), actual.size());
        assertTrue(getHeaderStream(response).anyMatch(h -> h.equals(expected)));
    }
    
    @Test
    public void getOne_ok() throws IOException {
        String fileName = "pl";
        String content = "Zażółć gęślą jaźń!";
        
        Mockito.when(fp.getFileInfo(fileName)).thenReturn(files.get(3));
        Mockito.when(fp.getFileContent(fileName)).thenReturn(content);
        HttpHeader expected = lastModifiedHeader(files.get(3).getModificationTime());
        
        HttpResponse response = controller.get(fileName);
        
        assertTrue(response instanceof OkResponse);
        assertEquals(content, response.getContent());
        assertTrue(getHeaderStream(response).anyMatch(h -> h.equals(expected)));
    }
}
