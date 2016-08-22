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

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Stream;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.Context;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.responses.HttpResponse;
import spark.Spark;

public class StringsController extends ControllerBase {
    
    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {
            Spark.get(rootUrl(), (req, res) -> {
                RequestContext rc = req.attribute(Application.REQUEST_CONTEXT_KEY);
                StringsController sc = new StringsController(rc);
                return render(sc.get(), res);
            }, gson::toJson);

            Spark.get(rootUrl()+"/:lang", (req, res) -> {
                RequestContext rc = req.attribute(Application.REQUEST_CONTEXT_KEY);
                StringsController sc = new StringsController(rc);
                return render(sc.get(req.params(":lang")), res);
            });
        }
        
        @Override
        public String rootUrl() {
            return super.rootUrl() + "/strings";
        }
        
    }
    
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private String path;
    
    public StringsController(RequestContext rc) throws Exception {
        this(rc, Application.getStringsDir());
    }
    
    public StringsController(RequestContext rc, String path) {
        super(rc);
        this.path = path;
    }
    
    public HttpResponse get() throws Exception {
        File directory = new File(path);
        File[] files = directory.listFiles();
        Date serverModification = Stream.concat(Collections.singleton(directory).stream(), 
                Stream.of(files))
                .map(f -> new Date(f.lastModified()))
                .max((d1, d2) -> d1.compareTo(d2))
                .orElse(Application.EMPTY_RESPONSE_MODIFICATION_DATE);
        String[] content = directory.list();
        return okCached(content, serverModification);
    }
    
    public HttpResponse get(String lang) throws IOException {
        File file = new File(path, lang);
        Date serverModification = new Date(file.lastModified());
        String content = new String(Files.readAllBytes(file.toPath()), CHARSET);
        return okCached(content, serverModification);
    }
}
