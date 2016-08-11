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
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.model.ApplicationSettings;

public class Context {
    
    private static final ThreadLocal<Context> instance = new ThreadLocal<Context>(){
        @Override
        protected Context initialValue() {
            return new Context();
        }
    };
    public static Context getInstance() {
        return instance.get();
    }
   
    private final EntityManager em;
    private final ApplicationSettings settings;
    private final Gson gson;
    
    private Context() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("release");
        em = emf.createEntityManager();
        TypedQuery<ApplicationSettings> tq = em.createQuery("Select x from ApplicationSettings x", ApplicationSettings.class);
        tq.setMaxResults(1);
        settings = tq.getSingleResult();
        gson = new Gson();
    }
    
    public EntityManager getEntityManager() {
        return em;
    }
    
    public String getSalt() {
        return settings.getSalt();
    }
    
    public boolean isInDevMode() {
        return true;
    }
    
    public Gson getGson() {
        return gson;
    }
}
