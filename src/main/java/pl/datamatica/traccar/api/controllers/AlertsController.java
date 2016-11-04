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

import java.util.List;
import java.util.stream.Collectors;
import pl.datamatica.traccar.api.Application;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.dtos.out.AlertDto;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.model.DeviceEvent;
import spark.Spark;

public class AlertsController extends ControllerBase {
    public static class Binder extends ControllerBinder {
        @Override
        public void bind() {
            Spark.get(rootUrl(), (req, res) -> {
                RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
                AlertsController ac = new AlertsController(context);
                return render(ac.get(), res);
            }, gson::toJson);
        }

        @Override
        public String rootUrl() {
            return super.rootUrl() + "/alerts";
        }
        
        
    }
    
    private final RequestContext rc;
    
    public AlertsController(RequestContext context) {
        super(context);
        rc = context;
    }
    
    public HttpResponse get() {
        List<DeviceEvent> events = rc.getAlertProvider().getAllAvailableAlerts();
        return okCached(events.stream()
                .filter(e -> isModified(e.getTime()))
                .map(e -> new AlertDto.Builder().event(e).build())
                .collect(Collectors.toList()));
    }
}
