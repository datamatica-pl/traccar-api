/*
 *  Copyright (C) 2017 Datamatica (dev@datamatica.pl)
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.providers.AuditLogProvider;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.responses.HttpResponse;
import spark.Request;
import spark.Spark;

/**
 *
 * @author piotrkrzeszewski
 */
public class AuditLogController extends ControllerBase {
    
    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {

            Spark.get(baseUrl(), (req, res) -> {
                AuditLogController alc = createController(req);
                return render(alc.get(req.queryMap().toMap()), res);
            }, gson::toJson);
            
            Spark.get(baseUrl()+"/:id", (req, res) -> {
                AuditLogController alc = createController(req);
                return render(alc.get(Long.parseLong(req.params(":id"))), res);
            }, gson::toJson);
        }

        private AuditLogController createController(Request req) {
            RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
            AuditLogController alc = new AuditLogController(context);
            return alc;
        }
        
        public String baseUrl() {
            return super.rootUrl() + "/auditlog";
        }
    }

    AuditLogProvider provider;
    
    public AuditLogController(RequestContext requestContext) {
        super(requestContext);
        provider = requestContext.getAuditLogProvider();
    }
    
    public HttpResponse get(Map<String, String[]> params) throws ProviderException {
        try {
            Map<String, Date> parsed = parseGetParams(params);

            return ok(provider.get(parsed.containsKey("fromDate") ? parsed.get("fromDate") : null,
                                   parsed.containsKey("fromDate") ? parsed.get("fromDate") : null));
        } catch (ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse get(long id) throws ProviderException {
        try {
            // TODO: Add some DTO
            return ok(provider.get(id)); 
        } catch (ProviderException e) {
            return handle(e);
        }
    }
    
    private Map<String, Date> parseGetParams(Map<String, String[]> params) {
        HashMap<String, Date> result = new HashMap();
        
        if (params == null || params.isEmpty())
            return result;
        
        if (params.containsKey("fromDate")) {
            DateFormat df = new SimpleDateFormat(Application.DATE_FORMAT);
            try {
                result.put("fromDate", df.parse(params.get("fromDate")[0]));
            }
            catch(ArrayIndexOutOfBoundsException | ParseException pe) { }
        }
        
        if (params.containsKey("toDate")) {
            DateFormat df = new SimpleDateFormat(Application.DATE_FORMAT);
            try {
                result.put("toDate", df.parse(params.get("toDate")[0]));
            }
            catch(ArrayIndexOutOfBoundsException | ParseException pe) { }
        }
        
        return result;
    }
    
}
