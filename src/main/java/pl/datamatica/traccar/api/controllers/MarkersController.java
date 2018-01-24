/*
 *  Copyright (C) 2016  Datamatica (dev@datamatica.pl)
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *  
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *  
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pl.datamatica.traccar.api.controllers;

import java.io.IOException;
import pl.datamatica.traccar.api.Application;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.providers.ImageProvider;
import spark.Request;
import spark.Response;
import spark.Spark;

public class MarkersController extends ControllerBase {
    
    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {
            Spark.get(baseUrl()+"/:name", (req, res) -> {
                MarkersController mc = createController(req);

                byte[] imageData = mc.getRawImageData(req.params(":name"));

                return writeResponse(res, "png", imageData);
            });
            
            Spark.get(baseUrl()+"/custom/:id", (req, res) -> {
                MarkersController mc = createController(req);
                
                byte[] imageData = mc.getRawCustom(Long.parseLong(req.params(":id")));
                
                return writeResponse(res, "png", imageData);
            });
            
            Spark.get(baseUrl()+"/vec/:name", (req, res) -> {
                MarkersController mc = createController(req);
                
                byte[] imageData = mc.getVecImageData(req.params(":name"));
                return writeResponse(res, "svg+xml", imageData);
            });
        }
        
        private Object writeResponse(Response res, String type, byte[] imageData) throws IOException {
            if(imageData == null)
                return gson.toJson(render(ControllerBase.notFound(), res));
            res.raw().setContentType("image/"+type+";charset=utf-8");
            // By using OutputStream maximum size of image if 64kB. 
            // To provide bigger images data should be wrote in loop (max 64kB at once).
            res.raw().getOutputStream().write(imageData, 0, imageData.length);
            return res;
        }

        private MarkersController createController(Request req) throws Exception {
            RequestContext rc = req.attribute(Application.REQUEST_CONTEXT_KEY);
            return new MarkersController(rc);
        }
        
        public String baseUrl() {
            return resourcesUrl() + "/markers";
        }
    }
    
    private ImageProvider imageProvider;
    
    public MarkersController(RequestContext rc) throws Exception {
        super(rc);
        imageProvider = rc.getImageProvider();
    }
    
    
    public byte[] getRawImageData(String iconName) {
        try {
            return imageProvider.getMarker(iconName);
        } catch(IOException e) {
            return null;
        }
    }
    
    private byte[] getRawCustom(long id) {
        try {
            return imageProvider.getCustomMarker(id);
        } catch(IOException e) {
            return null;
        }
    }
    
    private byte[] getVecImageData(String iconName) {
        try {
            return imageProvider.getVecMarker(iconName);
        } catch(IOException e) {
            return null;
        }
    }
}
