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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import pl.datamatica.traccar.api.auth.BasicAuthFilter;
import pl.datamatica.traccar.api.auth.PasswordValidator;
import pl.datamatica.traccar.api.controllers.*;
import spark.Spark;


public class Application implements spark.servlet.SparkApplication {
    
    public static final String REQUEST_CONTEXT_KEY = "pl.datamatica.traccar.api.RequestContext";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";
    public static final Date EMPTY_RESPONSE_MODIFICATION_DATE = new Date(1000);
    
    private final ControllerBinder[] BINDERS = new ControllerBinder[] {
            new DevicesController.Binder(),
            new UsersController.Binder()
        };
    
    @Override
    public void init() {
        Spark.get("test", (req, res) -> {
                return "Hello world";
        });
        PasswordValidator passValidator = new PasswordValidator();
        BasicAuthFilter baf = new BasicAuthFilter(passValidator);
        
        Spark.before((req, res) -> {
            RequestContext rc = new RequestContext(req, res);
            req.attribute(REQUEST_CONTEXT_KEY, rc);
            baf.handle(req, res);
        });
        
        Spark.after((req, res)-> {
            ((RequestContext)req.attribute(REQUEST_CONTEXT_KEY)).close();
        });
        
        for(ControllerBinder binder : BINDERS) 
            binder.bind();
        
        if(Context.getInstance().isInDevMode()) {
            Spark.exception(Exception.class, (exception, request, response) -> {
                StringWriter sw = new StringWriter();
                exception.printStackTrace(new PrintWriter(sw));
                response.status(500);
                response.type("text/plain");
                response.body(sw.toString());
            });
        }
    }
}