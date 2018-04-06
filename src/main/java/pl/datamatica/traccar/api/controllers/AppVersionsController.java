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
import pl.datamatica.traccar.api.Application;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.dtos.out.AppVersionsDto;
import pl.datamatica.traccar.api.dtos.in.EditAppVersionsDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.providers.AppVersionsProvider;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.responses.HttpResponse;
import spark.Spark;
/**
 *
 * @author piotrkrzeszewski
 */
public class AppVersionsController extends ControllerBase {
    
   public static class Binder extends ControllerBinder {
        @Override
        public void bind() {
            Spark.get(rootUrl(), (req, res) -> {
                RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
                AppVersionsController avc = new AppVersionsController(context);
                return render(avc.get(), res);
            }, gson::toJson);
            
            Spark.put(rootUrl(), (req, res) -> {
                RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
                AppVersionsController avc = new AppVersionsController(context);
                EditAppVersionsDto eavDto = gson.fromJson(req.body(), EditAppVersionsDto.class);
                return render(avc.put(eavDto), res);
            }, gson::toJson);
        }

        @Override
        public String rootUrl() {
            return super.rootUrl() + "/appversions";
        }
    }
   
    private final RequestContext rc;
    private final AppVersionsProvider provider;

    public AppVersionsController(RequestContext context) {
        super(context);
        rc = context;
        provider = rc.getAppVersionsProvider();
        provider.setRequestUser(rc.getUser());
    }
    
    public HttpResponse get() {
        AppVersionsDto dto = new AppVersionsDto.Builder().appVersions((provider.get())).build(); 
        return ok(dto);
    }
    
    public HttpResponse put(EditAppVersionsDto editDto) throws ProviderException {
        List<ErrorDto> errors = EditAppVersionsDto.validate(editDto);
        if(!errors.isEmpty())
            return badRequest(errors);
        try {
            provider.editAppVersions(editDto);
            return ok("");
        } catch (ProviderException e) {
            return handle(e);
        }
    }
}