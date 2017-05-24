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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.metadata.model.ImeiNumber;
import pl.datamatica.traccar.api.providers.ImeiProvider;
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
            freeMarkerConfiguration.setDefaultEncoding("utf-8");
            freeMarkerEngine.setConfiguration(freeMarkerConfiguration);
            
            Spark.get(baseUrl() + "/imeis", (req, res) -> {
                final RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
                final ImeiProvider imp = context.getImeiProvider();
                Map<String, Object> attributes = new HashMap<>();
                
                // TODO: Check privileges
                // TODO: Log IMEI
                
                attributes.put("imeis", imp.getAllImeis());
                
                res.status(HttpStatuses.OK);
                return freeMarkerEngine.render(new ModelAndView(attributes, "imei_manager.ftl"));
            });
            
            Spark.delete(baseUrl() + "/imei/:imeiId", (req, res) -> {
                final RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
                final ImeiProvider imp = context.getImeiProvider();
                final long imeiId = Long.valueOf(req.params(":imeiId"));
                
                // TODO: Check privileges
                // TODO: Log IMEI
                
                try {
                    ImeiNumber imei = imp.getImeiById(imeiId);
                    if (imei != null) {
                        imei.setIsDeleted(true);
                        return imei.getImei() + " zastał poprawnie usunięty.";
                    } else {
                        res.status(HttpStatuses.NOT_FOUND);
                        return "IMEI nie został znaleziony";
                    }
                } catch (Exception e) {
                    // TODO: Log error
                    res.status(HttpStatuses.BAD_REQUEST);
                    return "Wystąpił błąd przy kasowaniu numeru IMEI. Proszę odświeżyć " +
                            "okno przeglądarki i spróbować ponownie.";
                }
                
            });
            
            Spark.post(baseUrl() + "/imei/", (req, res) -> {
                final RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
                final ImeiProvider imp = context.getImeiProvider();
                final String imeiStr = req.queryParams("imeiNumber").trim();
                ImeiNumber imei;
                String successMsg;
                
                if (imeiStr == null || imeiStr.equals("")) {
                    return "Numer IMEI nie może być pusty.";
                }
                
                try {
                    context.disableSoftDeleteForMetadata(); // Allow deleted IMEI's to be retrieved,
                                                            // because if we find one it will be restored
                    imei = imp.getImeiByImeiString(imeiStr);
                } catch (Exception e) {
                    res.status(HttpStatuses.BAD_REQUEST);
                    return "Wystąpił błąd przy dodawaniu numeru IMEI, proszę spróbować ponownie.";
                }
                
                if (imei == null) {
                    imei = new ImeiNumber();
                    imei.setImei(imeiStr);
                    successMsg = String.format("IMEI %s został poprawnie dodany do bazy. " +
                                    "Proszę pamiętać o wpisaniu go do faktury.", imei.getImei());
                } else if (Objects.equals(imei.getIsDeleted(), Boolean.TRUE)) {
                    imei.setIsDeleted(Boolean.FALSE);
                    successMsg = String.format("IMEI %s istniał już w bazie ale był skasowany, został przywrócony. " +
                                    "Proszę pamiętać o wpisaniu go do faktury.", imei.getImei());
                } else {
                    res.status(HttpStatuses.CONFLICT);
                    return String.format("IMEI %s istnieje już w bazie.", imei.getImei());
                }
                
                try {
                    imp.saveImeiNumber(imei);
                    res.status(HttpStatuses.OK);
                    // TODO: Log
                    return successMsg;
                } catch (Exception e) {
                    res.status(HttpStatuses.BAD_REQUEST);
                    // TODO: Log
                    return "Wystąpił błąd przy dodawaniu numeru IMEI, proszę spróbować ponownie.";
                }
            });
        }

        public String baseUrl() {
            return "imei_manager";
        }
    }

    public ImeisController(RequestContext rc) {
        super(rc);
    }
}
