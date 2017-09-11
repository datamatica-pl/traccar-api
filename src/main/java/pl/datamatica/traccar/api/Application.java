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

import java.awt.Image;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import pl.datamatica.traccar.api.controllers.*;
import pl.datamatica.traccar.api.auth.BasicAuthFilter;
import pl.datamatica.traccar.api.controllers.RequestContext;
import pl.datamatica.traccar.api.fcm.AlarmDaemon;
import pl.datamatica.traccar.api.fcm.Daemon;
import pl.datamatica.traccar.api.fcm.SubscriptionDaemon;
import pl.datamatica.traccar.api.providers.ApplicationSettingsProvider;
import pl.datamatica.traccar.api.providers.ImageProvider;


public class Application implements spark.servlet.SparkApplication {

    public static final String REQUEST_CONTEXT_KEY = "pl.datamatica.traccar.api.RequestContext";
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
            new DeviceModelsController.Binder(),
            new DeviceIconsController.Binder(),
            new CommandsController.Binder(),
            new ImagesController.Binder(),
            new AlertsController.Binder(),
            new NotificationSettingsController.Binder(),
            new ImeisController.Binder(),
            new MarkersController.Binder(),
            new AppVersionsController.Binder(),
            new ApplicationSettingsController.Binder(),
            new DeviceGroupController.Binder(),
            new UserGroupsController.Binder(),
            new AuditLogController.Binder(),
            new ReportsController.Binder()
        };
    
    private final Daemon[] DAEMONS = new Daemon[]{
            new AlarmDaemon(),
            new SubscriptionDaemon()
        };

    @Override
    public void init() {
        BasicAuthFilter baf = new BasicAuthFilter();
        try {
            File f = new File(getImagesDir(), "empty_marker.png");
            Image emptyMarker = ImageIO.read(f);
            ImageProvider.setEmptyMarker(emptyMarker);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(Application.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        
        // Set static files. Root is 'src/main/resources', so put files in 'src/main/resources/public'
        Spark.staticFiles.location("/public");

        Spark.before((req, res) -> {
            RequestContext rc = new RequestContext(req, res);
            rc.beginTransaction();
            if (rc.isRequestForMetadata(req)) {
                rc.beginMetadataTransaction();
            }
            req.attribute(REQUEST_CONTEXT_KEY, rc);
            baf.handle(req, res);
            if (!BasicAuthFilter.shouldPassErrorsToController(req)) {
                if (req.attribute(RequestContext.REQUEST_FIELD_IS_AUTH) != null
                        && (Boolean)req.attribute(RequestContext.REQUEST_FIELD_IS_AUTH) == false) {
                    baf.unauthorized(res, req.attribute(RequestContext.REQUEST_FIELD_ERROR_DTO));
                }
            }
            //uncomment for debug
            //res.header("Access-Control-Allow-Origin", "http://127.0.0.1:8888");
            res.header("Cache-Control", "max-age=10");
        });
        
        //uncomment for debug
//        Spark.options("/*", (req, res) -> {
//            res.header("Access-Control-Allow-Methods", "GET, POST");
//            res.header("Access-Control-Allow-Headers", "Content-Type,"
//                    + "x-http-method-override,Authorization");
//            res.header("Access-Control-Max-Age", "86400");
//            res.body("");
//            return res;
//        });

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
        
        ScheduledExecutorService scheduler = Context.getInstance().getDaemonExecutor();
        for(Daemon daemon : DAEMONS) 
            daemon.start(scheduler);
        
        EntityManager em = Context.getInstance().createEntityManager();
        ApplicationSettingsProvider asp = new ApplicationSettingsProvider(em);
        if(asp.get().isEventRecordingEnabled())
            EventDaemon.getInstance().start(scheduler);

        if(Context.getInstance().isInDevMode()) {
            Spark.exception(Exception.class, (exception, request, response) -> {
                Logger logger = LoggerFactory.getLogger(Application.class);
                logger.error("Exception while processing "+request.servletPath(), exception);
                try {
                    RequestContext rc = (RequestContext)request.attribute(REQUEST_CONTEXT_KEY);
                    rc.rollbackTransaction();
                    if (rc.isRequestForMetadata(request)) {
                        rc.rollbackMetadataTransaction();
                    }
                    rc.close();
                } catch (Exception e) {
                    logger.error("Unable to close resources (EntityManager): ", e);
                }
                StringWriter sw = new StringWriter();
                exception.printStackTrace(new PrintWriter(sw));
                response.status(500);
                response.type("text/plain");
                response.body("Server error. Please contact us.");
            });
        }
    }

    public static String getStringsDir() throws Exception {
        return getConfigRecord(STRINGS_DIR_NAME);
    }

    public static String getImagesDir() throws Exception {
        return getConfigRecord(IMAGES_DIR_NAME);
    }
    
    public static String getConfigRecord(String key) throws Exception {
        InitialContext context = new InitialContext();
        return (String)context.lookup(key);
    }
}   