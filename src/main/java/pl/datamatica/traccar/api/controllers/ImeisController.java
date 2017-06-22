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
import pl.datamatica.traccar.api.dtos.ImeiNumberDto;
import pl.datamatica.traccar.api.metadata.model.ImeiNumber;
import pl.datamatica.traccar.api.providers.DbLog;
import pl.datamatica.traccar.api.providers.ImeiProvider;
import pl.datamatica.traccar.api.responses.HttpStatuses;
import spark.ModelAndView;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;
import org.slf4j.Logger;
import pl.datamatica.traccar.api.validators.MetadataValidator;

/**
 *
 * @author Jan Usarek
 */
public class ImeisController extends ControllerBase {
    public static class Binder extends ControllerBinder {
        private static final Logger dbLogger = DbLog.getLogger();
        
        @Override
        public void bind() {
            
            Configuration freeMarkerConfiguration = new Configuration(Configuration.VERSION_2_3_23);
            freeMarkerConfiguration.setTemplateLoader(new ClassTemplateLoader(Application.class, "/"));
            freeMarkerConfiguration.setDefaultEncoding("utf-8");
            FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine(freeMarkerConfiguration);
            
            Spark.get(baseUrl() + "/imeis", (req, res) -> {
                final RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
                final ImeiProvider imp = context.getImeiProvider();
                Map<String, Object> attributes = new HashMap<>();
                
                attributes.put("imeis", imp.getAllImeis());
                
                res.status(HttpStatuses.OK);
                return freeMarkerEngine.render(new ModelAndView(attributes, "imei_manager.ftl"));
            });
            
            Spark.delete(baseUrl() + "/imei/:imeiId", (req, res) -> {
                final RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
                final ImeiProvider imp = context.getImeiProvider();
                final long imeiId = Long.valueOf(req.params(":imeiId"));
                
                ImeiNumber imei = imp.getImeiById(imeiId);
                
                if (imei != null) {
                    imei.setIsDeleted(true);
                    imp.saveImeiNumber(imei);
                    
                    dbLogger.info(getLogMsgBegin(imei.getImei()) + " has been softly deleted.");
                    return imei.getImei() + " zastał poprawnie usunięty.";
                } else {
                    res.status(HttpStatuses.NOT_FOUND);
                    return "IMEI nie został znaleziony";
                }
            });
            
            Spark.put(baseUrl() + "/imei/:imeiId", (req, res) -> {
                final String jsonStr = req.body();
                final RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
                final ImeiProvider imp = context.getImeiProvider();
                final long imeiId = Long.valueOf(req.params(":imeiId"));

                ImeiNumberDto imeiDto = gson.fromJson(jsonStr, ImeiNumberDto.class);
                imeiDto.trimAllStrings();
                
                ImeiNumber imei = imp.getImeiById(imeiId);

                if (imei != null && !imei.getIsDeleted()) {
                    imp.updateImeiNumber(imei, imeiDto);
                    imp.saveImeiNumber(imei);
                    
                    dbLogger.info(getLogMsgBegin(imei.getImei()) + " has been updated.");
                    return imei.getImei() + " zastał poprawnie zmodyfikowany";
                } else {
                    res.status(HttpStatuses.NOT_FOUND);
                    return "IMEI nie został znaleziony";
                }
            });
            
            Spark.post(baseUrl() + "/imei/", (req, res) -> {
                final String jsonStr = req.body();
                final RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
                final ImeiProvider imp = context.getImeiProvider();
                final MetadataValidator mdv = new MetadataValidator();
                ImeiNumber imei;
                String successMsg;
                String logMsg;
                
                ImeiNumberDto imeiDto = gson.fromJson(jsonStr, ImeiNumberDto.class);
                imeiDto.trimAllStrings();
                
                if (!mdv.isImeiValid(imeiDto.getImei())) {
                    res.status(HttpStatuses.NOT_ACCEPTABLE);
                    return "Numer IMEI jest nieprawidłowy.";
                }
                
                context.disableSoftDeleteForMetadata(); // Allow deleted IMEI's to be retrieved,
                                                        // because if we find one it will be restored
                
                imei = imp.getImeiByImeiString(imeiDto.getImei());
                
                if (imei == null) {
                    imei = new ImeiNumber();
                    imei = imp.setNewImeiNumber(imei, imeiDto);
                    
                    logMsg = getLogMsgBegin(imei.getImei()) + " has been added.";
                    successMsg = String.format("IMEI %s został poprawnie dodany do bazy. " +
                                    "Proszę pamiętać o wpisaniu go do faktury.", imei.getImei());
                } else if (Objects.equals(imei.getIsDeleted(), Boolean.TRUE)) {
                    imei.setIsDeleted(Boolean.FALSE);
                    // Update values with new ones when restoring deleted IMEI
                    imp.updateImeiNumber(imei, imeiDto);
                    
                    logMsg = getLogMsgBegin(imei.getImei()) + " has been restored.";
                    successMsg = String.format("IMEI %s istniał już w bazie ale był skasowany, został przywrócony. " +
                                    "Proszę pamiętać o wpisaniu go do faktury.", imei.getImei());
                } else {
                    res.status(HttpStatuses.CONFLICT);
                    return String.format("IMEI %s istnieje już w bazie.", imei.getImei());
                }

                imp.saveImeiNumber(imei);

                res.status(HttpStatuses.OK);
                dbLogger.info(logMsg);

                return successMsg;
            });

            Spark.head(baseUrl() + "/imei/:imeiStr", (req, res) -> {
                final String imeiStr = req.params(":imeiStr");
                final RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
                final ImeiProvider imp = context.getImeiProvider();

                if (imp.isImeiRegisteredLocally(imeiStr)) {
                    res.status(HttpStatuses.OK);
                } else {
                    res.status(HttpStatuses.NOT_FOUND);
                }

                return "";
            });

        }
        
        public String baseUrl() {
            return "imei_manager";
        }
        
        private String getLogMsgBegin(String imeiStr) {
            return "ImeiManager: IMEI " + imeiStr;
        }
    }

    public ImeisController(RequestContext rc) {
        super(rc);
    }
}
