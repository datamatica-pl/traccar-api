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

import com.google.gson.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.datamatica.traccar.api.dtos.AnnotationExclusionStrategy;

public class Context {
    private static final Context INSTANCE = new Context();
    private final String PRODUCTION_TRACCAR_CONFIG_FILE = "/opt/traccar/conf/traccar.xml";
    private final String DEV_TRACCAR_CONFIG_FILE = "debug.xml";
    
    public static Context getInstance() {
        return INSTANCE;
    }
    
    private final EntityManagerFactory emf;
    private final EntityManagerFactory emfMetadata;
    private final Gson gson;
    
    private Context() {
        emf = Persistence.createEntityManagerFactory("release");
        Map<String, String> properties = getApiConnectionData();

        if (properties.size() > 0) {
            // Use properties obtained from 'debug.xml' or PRODUCTION_TRACCAR_CONFIG_FILE if possible
            emfMetadata = Persistence.createEntityManagerFactory("traccar_api_metadata_persistence", properties);
        } else {
            // Otherwise settings from 'persistence.xml' will be used
            emfMetadata = Persistence.createEntityManagerFactory("traccar_api_metadata_persistence");
        }
        
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat(Application.DATE_FORMAT);
        gsonBuilder.setExclusionStrategies(new AnnotationExclusionStrategy());
        if(isInDevMode())
            gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();
    }
    
    public final boolean isInDevMode() {
        return true;
    }
    
    private Map<String, String> getApiConnectionData() {
        Map<String, String> properties = new HashMap<>();
        try {
            final Class<?> configClass;
            configClass = Class.forName("org.traccar.Config");
            Object configObject = configClass.newInstance();

            Method loadMethod = configClass.getMethod("load", String.class);
            Method getStringMethod = configClass.getMethod("getString", String.class);

            try {
                loadMethod.invoke(configObject, PRODUCTION_TRACCAR_CONFIG_FILE);
            } catch (Exception e1) {
                loadMethod.invoke(configObject, DEV_TRACCAR_CONFIG_FILE);
            }
            
            String driver = (String)getStringMethod.invoke(configObject, "api.database.driver");
            String url = (String)getStringMethod.invoke(configObject, "api.database.url");
            String password = (String)getStringMethod.invoke(configObject, "api.database.password");
            String user = (String)getStringMethod.invoke(configObject, "api.database.user");

            if (driver != null) {
                properties.put("hibernate.connection.driver_class", driver);
            }
            if (url != null) {
                properties.put("hibernate.connection.url", url);
            }
            if (user != null) {
                properties.put("hibernate.connection.username", user);
            }
            if (password != null) {
                properties.put("hibernate.connection.password", password);
            }
        } catch (Exception e) {
            String errMsg = String.format("Unable to get connection to API's metadata DB from config file"
                    + " (can't load %s nor %s): %s", PRODUCTION_TRACCAR_CONFIG_FILE,
                    DEV_TRACCAR_CONFIG_FILE, e.getMessage());
            Logger logger = LoggerFactory.getLogger(Application.class);
            logger.error(errMsg);
        }
        
        return properties;
    }

    public Gson getGson() {
        return gson;
    }
    
    public EntityManager createEntityManager() {
        return emf.createEntityManager();
    }
    
    public EntityManager createMetadataEntityManager() {
        return emfMetadata.createEntityManager();
    }
}
