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

import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.dtos.out.PositionDto;
import pl.datamatica.traccar.api.providers.PositionProvider;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.responses.HttpResponse;
import spark.Request;
import spark.Spark;

public class PositionsController extends ControllerBase {
    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {
            Spark.get(rootUrl() + "/:id", (req, res) -> {
                PositionsController controller = createController(req);
                return render(controller.get(Long.parseLong(req.params(":id"))), res);
            });
        }
        
        private PositionsController createController(Request req) {
            RequestContext rc = req.attribute(Application.REQUEST_CONTEXT_KEY);
            return new PositionsController(rc);
        }

        @Override
        public String rootUrl() {
            return super.rootUrl() + "/positions";
        }

        
    }
    
    private PositionProvider pp;
    
    public PositionsController(RequestContext rc) {
        super(rc);
        pp = rc.getPositionProvider();
    }
    
    public HttpResponse get(long id) throws ProviderException {
        try {
            return ok(new PositionDto.Builder().position(pp.get(id)).build());
        } catch(ProviderException e) {
            return handle(e);
        }
    }
}
