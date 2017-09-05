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
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.dtos.out.AppVersionsInfoDto;
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
                AppVersionsController ac = new AppVersionsController(context);
                return render(ac.get(), res);
            }, gson::toJson);
        }

        @Override
        public String rootUrl() {
            return super.rootUrl() + "/appversions";
        }
    }
   
    private final RequestContext rc;
    private AppVersionsInfoDto cached; //server must be restarted to access new settings so we can save it
    
    public AppVersionsController(RequestContext context) {
        super(context);
        rc = context;
    }
    
    public HttpResponse get() {
        
        if (cached == null) {
            try {
                AppVersionsInfoDto appVer = new AppVersionsInfoDto();
                appVer.setAndroidVersion(Application.getConfigRecord("java:/versions.android"));
                appVer.setAndroidRequired(Application.getConfigRecord("java:/versions.androidRequired"));
                appVer.setIosVersion(Application.getConfigRecord("java:/versions.ios"));
                appVer.setIosRequired(Application.getConfigRecord("java:/versions.iosRequired"));
                appVer.setMessageKey(Application.getConfigRecord("java:/versions.messageKey"));
                appVer.setLocalizedMessage(Application.getConfigRecord("java:/versions.messageLocalized"));
                appVer.setMessageUrl(Application.getConfigRecord("java:/versions.messageUrl"));

                cached = appVer;
            }
            catch (Exception e) {
                System.out.println(e);
                return ok(getDefault());
            }
        }
        return ok(cached);
    }
    
    public AppVersionsInfoDto getDefault() {
        AppVersionsInfoDto appVer = new AppVersionsInfoDto();
        appVer.setAndroidVersion("1.0.0");
        appVer.setAndroidRequired("1.0.0");
        appVer.setIosVersion("1.0.0");
        appVer.setIosRequired("1.0.0");
        return appVer;
    }
}
