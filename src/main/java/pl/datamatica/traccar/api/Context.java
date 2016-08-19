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
import javax.persistence.*;
import pl.datamatica.traccar.api.dtos.AnnotationExclusionStrategy;

public class Context {
    private static final Context INSTANCE = new Context();
    
    public static Context getInstance() {
        return INSTANCE;
    }
   
    private final EntityManagerFactory emf;
    private final Gson gson;
    
    private Context() {
        emf = Persistence.createEntityManagerFactory("release");
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
    
    public Gson getGson() {
        return gson;
    }
    
    public EntityManager createEntityManager() {
        return emf.createEntityManager();
    }
}
