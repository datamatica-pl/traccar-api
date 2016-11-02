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
import pl.datamatica.traccar.api.dtos.AnnotationExclusionStrategy;

public class Context {
    private static final Context INSTANCE = new Context();
    
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
            // Use properties obtained from 'debug.xml' or '/org/traccar/conf/traccar.xml' if possible
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
    
    public boolean isInDevMode() {
        return true;
    }
    
    private Map<String, String> getApiConnectionData() {
        Map<String, String> properties = new HashMap<>();
        String pass = null;
        try {
            final Class<?> configClass;
            configClass = Class.forName("org.traccar.Config");
            Object configObject = configClass.newInstance();

            Method loadMethod = configClass.getMethod("load", String.class);
            Method getStringMethod = configClass.getMethod("getString", String.class);

            try {
                loadMethod.invoke(configObject, "/opt/traccar/conf/traccar.xml");
            } catch (Exception e1) {
                loadMethod.invoke(configObject, "debug.xml");
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
            // TODO: Log exception
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
