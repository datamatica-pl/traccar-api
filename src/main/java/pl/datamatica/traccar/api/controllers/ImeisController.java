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

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.responses.HttpStatuses;
import spark.ModelAndView;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

/**
 *
 * @author Jan Usarek
 */
public class ImeisController extends ControllerBase {
    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {

            // TODO Try to change not to use of deprecated methods
            FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine();
            Configuration freeMarkerConfiguration = new Configuration();
            freeMarkerConfiguration.setTemplateLoader(new ClassTemplateLoader(Application.class, "/"));
            freeMarkerEngine.setConfiguration(freeMarkerConfiguration);
            
            Spark.get(rootUrl() + "/imei_manager", (req, res) -> {
                res.status(HttpStatuses.OK);
                res.type("text/html");
                
                return freeMarkerEngine.render(new ModelAndView(Application.class, "imei_manager.ftl"));
            });
            
            Spark.get(rootUrl() + "/imei_manager_js", (req, res) -> {
                res.status(HttpStatuses.OK);
                res.type("application/javascript");
                
                return freeMarkerEngine.render(new ModelAndView(Application.class, "imei_manager.js"));
            });
        }

        public String baseUrl() {
            return rootUrl() + "/imeis";
        }
    }

    public ImeisController(RequestContext rc) {
        super(rc);
    }
}
