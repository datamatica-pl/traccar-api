/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.datamatica.traccar.api.controllers;

import pl.datamatica.traccar.api.Application;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.providers.ImageProvider;
import spark.Request;
import spark.Spark;

/**
 *
 * @author piotrkrzeszewski
 */
public class ImagesController extends ControllerBase{
    
    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {
            Spark.get(rootUrl()+"/:lang", (req, res) -> {
                ImagesController sc = createController(req);
                return render(sc.get(req.params(":name")), res);
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
}
