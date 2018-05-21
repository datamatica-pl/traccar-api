/*
 *  Copyright (C) 2016  Datamatica (dev@datamatica.pl)
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *  
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *  
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pl.datamatica.traccar.api.fcm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import org.apache.http.client.methods.CloseableHttpResponse;
import pl.datamatica.traccar.api.Context;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.model.NotificationStatus;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserSession;

public abstract class Daemon {
    private final Runnable runnable;
    
    protected Daemon() {
        runnable = new Runnable() {
            @Override
            public void run() {
                EntityManager em = Context.getInstance().createEntityManager();
                try {
                    em.getTransaction().begin();
                    doWork(em);
                    em.getTransaction().commit();
                } catch(Exception e) {
                    Logger.getLogger(Daemon.class.getName())
                            .log(Level.SEVERE, null, e);
                } finally {
                    em.close();
                }
            }
        };
    }
    
    public abstract void start(ScheduledExecutorService scheduler);
    
    protected void start(ScheduledExecutorService scheduler, long delay, long period){
        scheduler.scheduleAtFixedRate(runnable, delay, period, TimeUnit.MINUTES);
    }
    
    protected abstract void doWork(EntityManager em);
    
    protected void sendToFcm(EntityManager em, FcmNotificationDto dto) {
        Gson gson = Context.getInstance().getGson();
        String message = gson.toJson(dto);
        String result = sendToFcm(message);
        boolean success = false;
        if(result != null) {
            try {
                JsonParser parser = new JsonParser();
                JsonObject o = parser.parse(result).getAsJsonObject();
                success = o.get("success").getAsInt() == 1;
                if(!success)
                    Logger.getLogger(Daemon.class.getName())
                        .log(Level.INFO, o.getAsJsonArray("results").toString());
            } catch(Exception e) {
                // Probably incorrect fcm scret -> incorrect response JSON
                Logger.getLogger(Daemon.class.getName()).log(Level.INFO, e.getLocalizedMessage());
            }
        }
        NotificationStatus ns = new NotificationStatus(dto.getTo(), dto.getKind(), success);
        em.persist(ns);
    }
    
    private String sendToFcm(String body) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost("https://fcm.googleapis.com/fcm/send");
        CloseableHttpResponse response = null;
        
        try {
            request.addHeader("Authorization", 
                    "key=" + Application.getConfigRecord("java:/fcm_secret"));
            request.addHeader("Content-Type", "application/json");

            request.setEntity(new StringEntity(body));
            response = client.execute(request);
            if(response.getStatusLine().getStatusCode() != 200)
                return EntityUtils.toString(response.getEntity());
            return EntityUtils.toString(response.getEntity());
        } catch (NamingException ne) {
            // probably config file doesn't have fcm secret
            Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ne);
            return null;
        } catch (Exception ex) {
            Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                if(response != null)
                    response.close();
                client.close();
            } catch (IOException ex) {
                Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void clearInactiveSessions(EntityManager em, User user) {
        Set<String> activeSessions = new HashSet<>(em
                .createNativeQuery("SELECT sessionId from JettySessions")
                .getResultList());
        List<UserSession> validSessions = new ArrayList<>();
        for(UserSession s : user.getSessions()) {
            if(activeSessions.contains(s.getSessionId()))
                validSessions.add(s);
        }
        user.setSessions(validSessions);
    }
    
    public static class Helper {
        public static long minutesToHourOfDay(int hour) {
            Calendar calendar = Calendar.getInstance();
            if(calendar.get(Calendar.HOUR_OF_DAY) >= hour)
                calendar.add(Calendar.DATE, 1);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            return (calendar.getTimeInMillis()-System.currentTimeMillis())/(60*1000);
        }
    }
}
