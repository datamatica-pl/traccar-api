/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.datamatica.traccar.api.controllers;

import java.io.FileInputStream;
import org.hibernate.annotations.NotFound;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.providers.ImageProvider;
import spark.Request;
import spark.Spark;
import spark.utils.IOUtils;

/**
 *
 * @author piotrkrzeszewski
 */
public class ImagesController extends ControllerBase{
    
    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {
            Spark.get(baseUrl()+"/:name", (req, res) -> {
                ImagesController ic = createController(req);

                byte[] imageData = ic.getRawImageData(req.params(":name"));

                if (imageData == null) {
                    return gson.toJson(render(ic.notFound(), res));
                }
                
                res.raw().setContentType("image/png;charset=utf-8");
                // By using OutputStream maximum size of image if 64kB. 
                // To provide bigger images data should be wrote in loop (max 64kB at once).
                res.raw().getOutputStream().write(imageData, 0, imageData.length);
                
                return res;
            });
        }

        private ImagesController createController(Request req) throws Exception {
            RequestContext rc = req.attribute(Application.REQUEST_CONTEXT_KEY);
            ImagesController ic = new ImagesController(rc);
            return ic;
        }
        
        public String baseUrl() {
            return resourcesUrl() + "/images";
        }
    }
    
    ImageProvider imageProvider;
    
    public ImagesController(RequestContext rc) throws Exception {
        super(rc);
        this.imageProvider = rc.getImageProvider();
    }
    
    private byte[] getRawImageData(String name) throws Exception {
        
        FileInputStream stream = imageProvider.getInputStreamForImage(name);
        if (stream == null) {
            return null;
        }
        else {
            return IOUtils.toByteArray(stream);
        }
    }
}
