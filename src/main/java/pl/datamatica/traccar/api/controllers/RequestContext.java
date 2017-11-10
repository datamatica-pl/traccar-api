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

import java.awt.Image;
import pl.datamatica.traccar.api.providers.SessionProvider;
import java.text.ParseException;
import java.util.Date;
import java.util.Scanner;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.Context;
import pl.datamatica.traccar.api.providers.AlertProvider;
import pl.datamatica.traccar.api.providers.ApplicationSettingsProvider;
import pl.datamatica.traccar.api.providers.AuditLogProvider;
import pl.datamatica.traccar.api.providers.DeviceIconProvider;
import pl.datamatica.traccar.api.providers.DeviceModelProvider;
import pl.datamatica.traccar.api.providers.DeviceProvider;
import pl.datamatica.traccar.api.providers.FileProvider;
import pl.datamatica.traccar.api.providers.GeoFenceProvider;
import pl.datamatica.traccar.api.providers.DeviceGroupProvider;
import pl.datamatica.traccar.api.providers.ImageProvider;
import pl.datamatica.traccar.api.providers.ImeiProvider;
import pl.datamatica.traccar.api.providers.MailSender;
import pl.datamatica.traccar.api.providers.MessageProvider;
import pl.datamatica.traccar.api.providers.NotificationSettingsProvider;
import pl.datamatica.traccar.api.providers.PicturesProvider;
import pl.datamatica.traccar.api.providers.PositionProvider;
import pl.datamatica.traccar.api.providers.ReportProvider;
import pl.datamatica.traccar.api.providers.UserGroupProvider;
import pl.datamatica.traccar.api.providers.UserProvider;
import pl.datamatica.traccar.api.reports.ReportGenerator;
import pl.datamatica.traccar.api.reports.ReportGeneratorFactory;
import pl.datamatica.traccar.api.utils.DateUtil;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.ReportType;
import pl.datamatica.traccar.model.User;
import spark.Request;
import spark.Response;
import spark.Session;

public class RequestContext implements AutoCloseable {
    
    public static final String REQUEST_FIELD_IS_AUTH = "isAuthorized";
    public static final String REQUEST_FIELD_ERROR_DTO = "errorDto";
    
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
    private ImageProvider images;
    private PositionProvider positions;
    private NotificationSettingsProvider notificationSettings;
    private SessionProvider sessionProvider;
    private DeviceGroupProvider deviceGroupProvider;
    private UserGroupProvider userGroupProvider;
    private AuditLogProvider auditLogProvider;

    private Image emptyMarker;
    
    public RequestContext(Request request, Response response) throws ParseException {
        if(request.headers(IF_MODIFIED_SINCE_HEADER) != null)
            this.ifModifiedSince = DateUtil.parseDate(request.headers(IF_MODIFIED_SINCE_HEADER));
        this.em = Context.getInstance().createEntityManager();
        if (this.isRequestForMetadata(request)) {
            this.emMetadata = Context.getInstance().createMetadataEntityManager();
            if (this.isRequestForImeiManager(request)) {
                enableSoftDeleteForMetadata();
            }
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
            devices = new DeviceProvider(em, user, getImeiProvider(), 
                    getDeviceGroupProvider(), getPicturesProvider());
        return devices;
    }
    
    public UserProvider getUserProvider() {
        ApplicationSettingsProvider appSettingsProvider = getApplicationSettingsProvider();
        if(users == null)
            users = new UserProvider(em, appSettingsProvider.get());
        return users;
    }
    
    public ApplicationSettingsProvider getApplicationSettingsProvider() {
        if(appSettings == null) {
            appSettings = new ApplicationSettingsProvider(em);
        }
        return appSettings;
    }
    
    public FileProvider getFileProvider() throws Exception {
        if(files == null) 
            files = new FileProvider(Application.getStringsDir());
        return files;
    }
    
    public ImageProvider getImageProvider() throws Exception {
        if(images == null) 
            images = new ImageProvider(Application.getImagesDir(), em);
        return images;
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
    
    public MailSender getMailSender() {
        return new MailSender(em);
    }
    
    public DeviceModelProvider getDeviceModelProvider() {
        DeviceModelProvider provider = new DeviceModelProvider(em, emMetadata);
        return provider;
    }
    
    public DeviceIconProvider getDeviceIconProvider() {
        DeviceIconProvider provider = new DeviceIconProvider(em, emMetadata);
        return provider;
    }
    
    public AlertProvider getAlertProvider() {
        AlertProvider provider = new AlertProvider(em, user);
        return provider;
    }
    
    public NotificationSettingsProvider getNotificationSettingsProvider() {
        if(notificationSettings == null)
            notificationSettings = new NotificationSettingsProvider(em, user);
        return notificationSettings;
    }

    public ImeiProvider getImeiProvider() {
        return new ImeiProvider(emMetadata);
    }
    
    public SessionProvider getSessionProvider() {
        if(sessionProvider == null)
            sessionProvider = new SessionProvider(em, user);
        return sessionProvider;
    }
    
    PicturesProvider getPicturesProvider() {
        return new PicturesProvider(em);
    }
    
    public DeviceGroupProvider getDeviceGroupProvider() {
        if (deviceGroupProvider == null) {
            deviceGroupProvider = new DeviceGroupProvider(em, user);
            if (devices == null) {
                devices = new DeviceProvider(em, user, getImeiProvider(), 
                        deviceGroupProvider, getPicturesProvider());
            }
            deviceGroupProvider.setDeviceProvider(devices);
        }
        return deviceGroupProvider;
    }
    
    public UserGroupProvider getUserGroupProvider() {
        if (userGroupProvider == null) {
            userGroupProvider = new UserGroupProvider(em, user);
            userGroupProvider.setUserProvider(getUserProvider());
            userGroupProvider.setApplicationSettingsProvider(getApplicationSettingsProvider());
        }
        return userGroupProvider;
    }
    
    public AuditLogProvider getAuditLogProvider() {
        if (auditLogProvider == null) {
            auditLogProvider = new AuditLogProvider(em, user);
        }
        return auditLogProvider;
    }
    
    public MessageProvider getMessageProvider(String lang) throws Exception {
        return new MessageProvider(em, Application.getStringsDir(), lang);
    }
    
    public ReportGenerator getReportGenerator(ReportType type, String lang) throws Exception {
        ReportGeneratorFactory factory = new ReportGeneratorFactory();
        ReportGenerator generator = factory.getGenerator(type);
        ApplicationSettings as = getApplicationSettingsProvider().get();
        if(lang == null)
            lang = as.getLanguage();
        else if("default".equalsIgnoreCase(lang))
            lang = "en";
        
        generator.setEntityManager(em);
        generator.setCurrentUser(user);
        generator.setDeviceProvider(getDeviceProvider());
        generator.setPositionProvider(getPositionProvider());
        generator.setGfProvider(getGeoFencesProvider());
        generator.setIconsProvider(getDeviceIconProvider());
        generator.setMsgProvider(getMessageProvider(lang));
        generator.setApplicationSettings(as);
        
        return generator;
    }
    
     public ReportProvider getReportProvider() {
        DeviceProvider dp = getDeviceProvider();
        GeoFenceProvider gfp = getGeoFencesProvider();
        return new ReportProvider(em, user, dp, gfp);
    }
    
    public String getApiRoot() {
        if(request == null)
            return "";
        String pattern = "^.*/v[0-9]+";
        Scanner scanner = new Scanner(request.url());
        return scanner.findInLine(pattern);
    }
    
    public String getServerRoot() {
        if(request == null)
            return "";
        String pattern = ".*?/";
        Scanner scanner = new Scanner(request.url());
        return scanner.findInLine(pattern);
    }
    
    public final boolean isRequestForMetadata(Request request) {
//        String pattern = "^/v[0-9]+/" + ControllerBinder.RESOURCES_URL_PREFIX + "/.*";
//        return request.servletPath().matches(pattern);
        // TODO: We need to talk whether we want to add calls to devices here or just remove it
        // and always add metadata EntityManager and open transaction on it. Temporary return true
        // to get IMEI list on devices.
        // Important: If we ever use is this method again, lets make sure, that request for imei_manager
        // are also included, as they have different url. IMEI's are metadata, and must have access to
        // its EntityManager
        return true;
    }
    
    public final boolean isRequestForImeiManager(Request request) {
        final String manager_uri_pattern = "^/imei_manager.*";
        return request.uri().matches(manager_uri_pattern);
    }
    
    public final String getRequestUri() {
        if (request.uri() != null) {
            return request.uri();
        } else {
            return "";
        }
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
    
    public void setEmptyMarkerImage(Image img) {
        this.emptyMarker = img;
    }

    public void beginTransaction() {
        em.getTransaction().begin();
    }

    public void commitTransaction() {
        EntityTransaction et = em.getTransaction();
        if (et != null && et.isActive()) {
            et.commit();
        }
    }
    
    public void rollbackTransaction() {
        EntityTransaction et = em.getTransaction();
        if (et != null && et.isActive()) {
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
            if (et != null && et.isActive()) {
                et.commit();
            }
        }
    }
    
    public void rollbackMetadataTransaction() {
        if (emMetadata != null) {
            EntityTransaction et = emMetadata.getTransaction();
            if (et != null && et.isActive()) {
                et.rollback();
            }
        }
    }
    
    public final void disableSoftDeleteForMetadata() {
        this.emMetadata.unwrap(org.hibernate.Session.class).disableFilter("softDelete");
    }
    
    public final void enableSoftDeleteForMetadata() {
        this.emMetadata.unwrap(org.hibernate.Session.class).enableFilter("softDelete");
    }
}
