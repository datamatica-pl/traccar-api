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

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import pl.datamatica.traccar.api.exceptions.ConfigLoadException;

/**
 *
 * @author Jan Usarek
 */
public class TraccarConfig {

    private static TraccarConfig INSTANCE = null;
    private final String DEV_TRACCAR_CONFIG_FILE = "debug.xml";
    // Traccar is started from script in 'bin' folder, so this is relative path to config file,
    // alternatively absolute path like '/opt/traccar/conf/traccar.xml' can also be used.
    private final String PRODUCTION_TRACCAR_CONFIG_FILE = "../conf/traccar.xml";

    private final Class<?> configClass;
    private final Object configObject;
    private final Method loadMethod;
    private final Method getStringMethod;

    private TraccarConfig() throws ClassNotFoundException, InstantiationException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            FileNotFoundException
    {
        configClass = Class.forName("org.traccar.Config");
        configObject = configClass.newInstance();
        loadMethod = configClass.getMethod("load", String.class);
        getStringMethod = configClass.getMethod("getString", String.class);
        
        File prodConfigFile = new File(PRODUCTION_TRACCAR_CONFIG_FILE);
        File devConfigFile = new File(DEV_TRACCAR_CONFIG_FILE);
        
        if (prodConfigFile.exists() && prodConfigFile.isFile()) {
            loadMethod.invoke(configObject, PRODUCTION_TRACCAR_CONFIG_FILE);
        } else if (devConfigFile.exists() && devConfigFile.isFile()) {
            loadMethod.invoke(configObject, DEV_TRACCAR_CONFIG_FILE);
        } else {
            throw new FileNotFoundException("Production nor develpment Traccar's config file" +
                    " has not been found");
        }
    }
    
    public static TraccarConfig getInstance() throws ConfigLoadException {
        try {
            INSTANCE = new TraccarConfig();
        } catch (ClassNotFoundException | InstantiationException | NoSuchMethodException |
                IllegalAccessException | InvocationTargetException | FileNotFoundException e)
        {
            throw new ConfigLoadException("Traccar config could not be loaded", e);
        }
        return INSTANCE;
    }
    
    public String getStringParam(String paramPath) throws IllegalAccessException, InvocationTargetException {
        return (String)getStringMethod.invoke(configObject, paramPath);
    }
}
