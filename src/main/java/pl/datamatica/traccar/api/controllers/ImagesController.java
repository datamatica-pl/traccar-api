/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.datamatica.traccar.api.controllers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import pl.datamatica.traccar.api.Application;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
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
            Spark.get(rootUrl()+"/:lang", (req, res) -> {
                ImagesController ic = createController(req);

                byte[] imageData = ic.getRawImageData(":name");
                res.raw().setContentType("image/png;charset=utf-8");
                res.raw().getOutputStream().write(imageData, 0, imageData.length);
                return res;
            });
        }

        private ImagesController createController(Request req) throws Exception {
            RequestContext rc = req.attribute(Application.REQUEST_CONTEXT_KEY);
            ImagesController ic = new ImagesController(rc);
            return ic;
        }
        
        @Override
        public String rootUrl() {
            return super.rootUrl() + "/images";
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
