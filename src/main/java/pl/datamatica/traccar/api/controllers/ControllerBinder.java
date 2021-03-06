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

import com.google.gson.Gson;
import pl.datamatica.traccar.api.Context;

public abstract class ControllerBinder {
    protected Gson gson;
    public static final String RESOURCES_URL_PREFIX = "resources";

    public ControllerBinder() {
        gson = Context.getInstance().getGson();
    }

    public String rootUrl() {
        return "v1";
    }
    
    public String resourcesUrl() {
        return rootUrl() + "/" + RESOURCES_URL_PREFIX;
    }

    public abstract void bind();
}
