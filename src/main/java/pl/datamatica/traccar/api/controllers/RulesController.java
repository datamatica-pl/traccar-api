/*
 *  Copyright (C) 2018  Datamatica (dev@datamatica.pl)
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *  
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *  
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pl.datamatica.traccar.api.controllers;

import com.google.gson.reflect.TypeToken;
import java.util.List;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.dtos.in.AddRulesDto;
import pl.datamatica.traccar.api.dtos.in.RulesAcceptanceDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.providers.RulesProvider;
import pl.datamatica.traccar.api.responses.HttpResponse;
import spark.Request;
import spark.Spark;

/**
 *
 * @author ŁŁ
 */
public class RulesController extends ControllerBase {
    
    public static class Binder extends ControllerBinder {
        @Override
        public void bind() {            
            Spark.put(baseUrl()+"/accept", (req, res) -> {
                RulesController rc = createController(req);
                RulesAcceptanceDto ra = gson.fromJson(req.body(), RulesAcceptanceDto.class);
                return render(rc.acceptVersions(ra), res);
            });
            
            Spark.post(baseUrl(), (req, res) -> {
                RulesController rc = createController(req);
                AddRulesDto dto = gson.fromJson(req.body(), AddRulesDto.class);
                return render(rc.post(dto), res);
            }, gson::toJson);
        }
        
        public RulesController createController(Request req) {
            RequestContext rc = req.attribute(Application.REQUEST_CONTEXT_KEY);
            return new RulesController(rc);
        }
        
        public String baseUrl() {
            return super.rootUrl() + "/rules";
        }
    }
    
    private final RulesProvider rp;
    
    public RulesController(RequestContext requestContext) {
        super(requestContext);
        this.rp = requestContext.getRulesProvider();
    }
    
    public HttpResponse post(AddRulesDto dto) throws ProviderException {
        List<ErrorDto> errors = AddRulesDto.validate(dto);
        if(!errors.isEmpty()) {
            return badRequest(errors);
        }
        
        try{
            rp.createRules(dto);
            return created("", "");
        } catch(ProviderException e) {
            return handle(e);
        }
    }
 
    public HttpResponse acceptVersions(RulesAcceptanceDto ra) throws ProviderException {
        try{
            for(long id : ra.getAccepted())
                rp.acceptVersion(id);
            for(long id : ra.getRejected())
                rp.rejectVersion(id);
            return ok("");
        } catch(ProviderException e) {
            return handle(e);
        }
    }
}
