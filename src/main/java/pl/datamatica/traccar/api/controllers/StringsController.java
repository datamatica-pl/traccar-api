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
import java.util.stream.Stream;
import pl.datamatica.traccar.api.Application;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.dtos.out.FileDto;
import pl.datamatica.traccar.api.providers.FileProvider;
import pl.datamatica.traccar.api.responses.HttpResponse;
import spark.Request;
import spark.Spark;

public class StringsController extends ControllerBase {
    
    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {
            Spark.get(rootUrl(), (req, res) -> {
                StringsController sc = createController(req);
                return render(sc.get(), res);
            }, gson::toJson);

            Spark.get(rootUrl()+"/:lang", (req, res) -> {
                StringsController sc = createController(req);
                return render(sc.get(req.params(":lang")), res);
            });
        }

        private StringsController createController(Request req) throws Exception {
            RequestContext rc = req.attribute(Application.REQUEST_CONTEXT_KEY);
            StringsController sc = new StringsController(rc);
            return sc;
        }
        
        @Override
        public String rootUrl() {
            return super.rootUrl() + "/strings";
        }
        
    }
    
    private final FileProvider fp;
    
    public StringsController(RequestContext rc) throws Exception {
        super(rc);
        this.fp = rc.getFileProvider();
    }
    
    public HttpResponse get() throws Exception {
        List<FileDto> files = fp.getAllFiles().collect(Collectors.toList());
        
        Date listLastModified = fp.getListLastModified();
        Date serverModification = Stream.concat(Stream.of(listLastModified), 
                files.stream().map(FileDto::getModificationTime))
                .max((d1, d2) -> d1.compareTo(d2))
                .orElse(Application.EMPTY_RESPONSE_MODIFICATION_DATE);
        
        return okCached(files, serverModification);
    }
    
    public HttpResponse get(String lang) throws IOException {
        Date serverModification = fp.getFileInfo(lang).getModificationTime();
        
        if(isModified(serverModification)) {
            String content = fp.getFileContent(lang);
            return okCached(content, serverModification);
        }
        return notModified(serverModification);
    }
}
