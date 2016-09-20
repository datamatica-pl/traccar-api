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

import java.text.ParseException;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.Context;
import pl.datamatica.traccar.api.providers.ApplicationSettingsProvider;
import pl.datamatica.traccar.api.providers.DeviceIconProvider;
import pl.datamatica.traccar.api.providers.DeviceModelProvider;
import pl.datamatica.traccar.api.providers.DeviceProvider;
import pl.datamatica.traccar.api.providers.FileProvider;
import pl.datamatica.traccar.api.providers.GeoFenceProvider;
import pl.datamatica.traccar.api.providers.PositionProvider;
import pl.datamatica.traccar.api.providers.ReportsProvider;
import pl.datamatica.traccar.api.providers.UserProvider;
import pl.datamatica.traccar.api.utils.DateUtil;
import pl.datamatica.traccar.model.User;
import spark.Request;
import spark.Response;
import spark.Session;

public class RequestContext implements AutoCloseable {
    
    private static final String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";
    
    private Date ifModifiedSince;
    
    private User user;
    private final EntityManager em;
    private final EntityManager emMetadata;
    private final Request request;
    
    private DeviceProvider devices;
    private UserProvider users;
    private ApplicationSettingsProvider appSettings;
    private FileProvider files;
    private PositionProvider positions;
    
    public RequestContext(Request request, Response response) throws ParseException {
        if(request.headers(IF_MODIFIED_SINCE_HEADER) != null)
            this.ifModifiedSince = DateUtil.parseDate(request.headers(IF_MODIFIED_SINCE_HEADER));
        this.em = Context.getInstance().createEntityManager();
        if (this.isRequestForMetadata(request)) {
            this.emMetadata = Context.getInstance().createMetadataEntityManager();
        } else {
            this.emMetadata = null;
        }
        this.request = request;
    }
    
    public Date getModificationDate() {
        return ifModifiedSince;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public DeviceProvider getDeviceProvider() {
        if(devices == null)
            devices = new DeviceProvider(em, user);
        return devices;
    }
    
    public UserProvider getUserProvider() {
        ApplicationSettingsProvider appSettingsProvider = getApplicationSettingsProvider();
        if(users == null)
            users = new UserProvider(em, appSettingsProvider.get());
        return users;
    }
    
    public ApplicationSettingsProvider getApplicationSettingsProvider() {
        if(appSettings == null)
            appSettings = new ApplicationSettingsProvider(em);
        return appSettings;
    }
    
    public FileProvider getFileProvider() throws Exception {
        if(files == null) 
            files = new FileProvider(Application.getStringsDir());
        return files;
    }
    
    public PositionProvider getPositionProvider() {
        if(positions == null)
            positions = new PositionProvider(em, user);
        return positions;
    }
    
    public GeoFenceProvider getGeoFencesProvider() {
        GeoFenceProvider provider = new GeoFenceProvider(em);
        provider.setRequestUser(user);
        return provider;
    }
    
    public ReportsProvider getReportsProvider() {
        ReportsProvider provider = new ReportsProvider(em, emMetadata);
        return provider;
    }
    
    public DeviceModelProvider getDeviceModelProvider() {
        DeviceModelProvider provider = new DeviceModelProvider(em, emMetadata);
        return provider;
    }
    
    public DeviceIconProvider getDeviceIconProvider() {
        DeviceIconProvider provider = new DeviceIconProvider(em, emMetadata);
        return provider;
    }
    
    public final boolean isRequestForMetadata(Request request) {
        String pattern = "^/v[0-9]+/" + ControllerBinder.RESOURCES_URL_PREFIX + "/.*";
        return request.servletPath().matches(pattern);
    }

    @Override
    public void close() throws Exception {
        em.close();
        if (emMetadata != null) {
            emMetadata.close();
        }
    }

    public Session session() {
        return request.session();
    }

    public void beginTransaction() {
        em.getTransaction().begin();
    }

    public void commitTransaction() {
        EntityTransaction et = em.getTransaction();
        if (et.isActive()) {
            et.commit();
        }
    }
    
    public void rollbackTransaction() {
        EntityTransaction et = em.getTransaction();
        if (et.isActive()) {
            et.rollback();
        }
    }
    
    public void beginMetadataTransaction() {
        if (emMetadata != null) {
            emMetadata.getTransaction().begin();
        }
    }
    
    public void commitMetadataTransaction() {
        if (emMetadata != null) {
            EntityTransaction et = emMetadata.getTransaction();
            if (et.isActive()) {
                et.commit();
            }
        }
    }
    
    public void rollbackMetadataTransaction() {
        if (emMetadata != null) {
            EntityTransaction et = emMetadata.getTransaction();
            if (et.isActive()) {
                et.rollback();
            }
        }
    }
}
