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
package pl.datamatica.traccar.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.regex.Pattern;
import javax.naming.InitialContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import pl.datamatica.traccar.api.controllers.*;
import pl.datamatica.traccar.api.auth.BasicAuthFilter;
import pl.datamatica.traccar.api.dtos.out.AppVersionsInfoDto;
import pl.datamatica.traccar.api.controllers.RequestContext;


public class Application implements spark.servlet.SparkApplication {

    public static final String REQUEST_CONTEXT_KEY = "pl.datamatica.traccar.api.RequestContext";
    public static String NOTIFICATION_TOKEN_SESSION_KEY = "notificationToken";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";
    public static final Date EMPTY_RESPONSE_MODIFICATION_DATE = new Date(1000);
    public static final Pattern COLOR_PATTERN = Pattern.compile("[0-9A-F]{6}");

    private static final String STRINGS_DIR_NAME = "java:/StringsDir";
    private static final String IMAGES_DIR_NAME = "java:/ImagesDir";
    private final ControllerBinder[] BINDERS = new ControllerBinder[] {
            new DevicesController.Binder(),
            new UsersController.Binder(),
            new StringsController.Binder(),
            new PositionsController.Binder(),
            new GeofencesController.Binder(),
            new SessionController.Binder(),
            new ReportsController.Binder(),
            new DeviceModelsController.Binder(),
            new DeviceIconsController.Binder(),
            new CommandsController.Binder(),
            new ImagesController.Binder()
        };

    @Override
    public void init() {
        BasicAuthFilter baf = new BasicAuthFilter();

        Spark.before((req, res) -> {
            RequestContext rc = new RequestContext(req, res);
            rc.beginTransaction();
            if (rc.isRequestForMetadata(req)) {
                rc.beginMetadataTransaction();
            }
            req.attribute(REQUEST_CONTEXT_KEY, rc);
            baf.handle(req, res);
        });

        Spark.get("v1/appversions", (req, res) -> {
            AppVersionsInfoDto appVer = new AppVersionsInfoDto();
            appVer.setAndroidVersion("0.1.0");
            appVer.setAndroidRequired("0.1.0");
            appVer.setIosVersion("0.3.0");
            appVer.setIosRequired("0.2.0");
            appVer.setMessageKey("devices_spring_promo");
            appVer.setLocalizedMessage("Wiosenna promocja");
            appVer.setMessageUrl("http://www.shop-angelgts.pl/spring/promo");
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder
                            .setPrettyPrinting()
                            .create();

            res.status(200);
            res.type("application/json");
            res.body(gson.toJson(appVer));

            return(res);
        });

        Spark.after((req, res)-> {
            RequestContext rc = (RequestContext)req.attribute(REQUEST_CONTEXT_KEY);
            rc.commitTransaction();
            if (rc.isRequestForMetadata(req)) {
                rc.commitMetadataTransaction();
            }
            rc.close();
        });

        for(ControllerBinder binder : BINDERS)
            binder.bind();

        if(Context.getInstance().isInDevMode()) {
            Spark.exception(Exception.class, (exception, request, response) -> {
                Logger logger = LoggerFactory.getLogger(Application.class);
                try {
                    RequestContext rc = (RequestContext)request.attribute(REQUEST_CONTEXT_KEY);
                    rc.rollbackTransaction();
                    if (rc.isRequestForMetadata(request)) {
                        rc.rollbackMetadataTransaction();
                    }
                    rc.close();
                } catch (Exception e) {
                    logger.error("Unable to close resources (EntityManager): " + e.getMessage());
                }
                StringWriter sw = new StringWriter();
                exception.printStackTrace(new PrintWriter(sw));
                response.status(500);
                response.type("text/plain");
                response.body(sw.toString());
            });
        }
    }

    public static String getStringsDir() throws Exception {
        InitialContext context = new InitialContext();
        return (String)context.lookup(STRINGS_DIR_NAME);
    }

    public static String getImagesDir() throws Exception {
        InitialContext context = new InitialContext();
        return (String)context.lookup(IMAGES_DIR_NAME);
    }
}