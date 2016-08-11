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
import pl.datamatica.traccar.api.auth.BasicAuthFilter;
import pl.datamatica.traccar.api.auth.PasswordValidator;
import pl.datamatica.traccar.api.controllers.DevicesController;
import spark.Spark;
import spark.utils.SparkUtils;


public class Application implements spark.servlet.SparkApplication {
    
    @Override
    public void init() {
        Spark.get("test", (req, res) -> {
                return "Hello world";
        });
        PasswordValidator passValidator = new PasswordValidator(Context.getInstance());
        Spark.before(new BasicAuthFilter(SparkUtils.ALL_PATHS, passValidator));
        
        DevicesController.registerMethods();
    }
}