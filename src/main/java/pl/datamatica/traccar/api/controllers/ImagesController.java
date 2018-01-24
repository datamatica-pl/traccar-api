/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.datamatica.traccar.api.controllers;

import java.io.FileInputStream;
import java.io.IOException;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.providers.ImageProvider;
import spark.Request;
import spark.Response;
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
            Spark.get(baseUrl()+"/vec/:name", (req, res) -> {
                ImagesController ic = createController(req);
                
                byte[] imageData = ic.getVecImageData(req.params(":name"));
                
                return writeResponse(res, "svg+xml", imageData);
            });
            
            Spark.get(baseUrl()+"/:name", (req, res) -> {                
                ImagesController ic = createController(req);
                
                byte[] imageData = ic.getRawImageData(req.params(":name"));
                
                return writeResponse(res, "png", imageData);
            });
        }
        
        private Object writeResponse(Response res, String contentType, byte[] imageData) throws IOException {
            if(imageData == null)
                return gson.toJson(render(ControllerBase.notFound(), res));
            res.raw().setContentType("image/"+contentType+";charset=utf-8");
            // By using OutputStream maximum size of image if 64kB. 
            // To provide bigger images data should be wrote in loop (max 64kB at once).
            res.raw().getOutputStream().write(imageData, 0, imageData.length);
            return res;
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
    
    private byte[] getVecImageData(String name) throws Exception {
        
        FileInputStream stream = imageProvider.getInputStreamForVector(name);
        if(stream == null)
            return null;
        else
            return IOUtils.toByteArray(stream);
    }
}
