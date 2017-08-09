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
package pl.datamatica.traccar.api.auth;

import com.google.gson.Gson;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.Context;
import pl.datamatica.traccar.api.TraccarConfig;
import spark.*;
import pl.datamatica.traccar.api.controllers.RequestContext;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.exceptions.ConfigLoadException;
import pl.datamatica.traccar.api.providers.ApplicationSettingsProvider;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.providers.UserProvider;
import pl.datamatica.traccar.api.responses.HttpStatuses;
import pl.datamatica.traccar.model.User;

public class BasicAuthFilter {
    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String SCHEME = "Basic";
    private static final String REALM = "traccar-api";
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final String AUTH_INFO_SEPARATOR = " ";
    public static final String USER_ID_SESSION_KEY = "userId";
    private Request req;
    
    private static final int SESSION_MAX_INACTIVE_INTERVAL = (int)TimeUnit.SECONDS.convert(14, TimeUnit.DAYS);
    
    private static final Logger logger = LoggerFactory.getLogger(BasicAuthFilter.class);
    
    public void handle(Request request, Response response) throws Exception {
        this.req = request;
        RequestContext rc = request.attribute(Application.REQUEST_CONTEXT_KEY);
        
        if(shouldAllowUnauthorized(request)) {
            request.attribute(RequestContext.REQUEST_FIELD_IS_AUTH, true);
            return;
        }
        
        // assuming that auth fails
        request.attribute(RequestContext.REQUEST_FIELD_IS_AUTH, false);

        try {
            UserProvider up = rc.getUserProvider();
            ApplicationSettingsProvider asp = rc.getApplicationSettingsProvider();
            User user;
            
            if(request.session().attributes().contains(USER_ID_SESSION_KEY))
                user = continueSession(request, up);
            else
                user = beginSession(request, up);
            if(user.isBlocked()) {
                request.attribute(RequestContext.REQUEST_FIELD_ERROR_DTO, new ErrorDto(MessageKeys.ERR_ACCOUNT_BLOCKED));
                return;
            } else if(user.isExpired()) {
                request.attribute(RequestContext.REQUEST_FIELD_IS_AUTH, new ErrorDto(MessageKeys.ERR_ACCOUNT_EXPIRED));
                return;
            }
            
            if (rc.isRequestForImeiManager(request)) {
                // Check whether IP is allowed to manage IMEI's
                boolean isIpAllowedToAddImei = false;
                boolean isImeiManagerEnabled = false;
                try {
                    final TraccarConfig traccarConf = TraccarConfig.getInstance();
                    final String[] allowedIps = traccarConf.getStringParam("api.imei_manager.allowed_ips").split(",");
                    
                    isIpAllowedToAddImei = Arrays.asList(allowedIps).contains(request.ip());
                    isImeiManagerEnabled = traccarConf.getBooleanParam("api.imei_manager.enabled");
                } catch (ConfigLoadException e) {
                    logger.error("Get allowed IP's from traccar config failed: " + e.getMessage(), e);
                }
                
                if (!isImeiManagerEnabled) {
                    logger.error("Trying to run IMEI manager, but it's disabled.");
                    notFound();
                }
                
                // Allow only IMEI manager for request to DM IMEI Manager
                if (!user.isImeiManager()) {
                    logger.error(String.format("User %s tried to access IMEI manager without permissions.", user.getLogin()));
                    request.attribute(RequestContext.REQUEST_FIELD_ERROR_DTO, new ErrorDto(MessageKeys.ERR_ACCESS_DENIED));
                    return;
                }
                
                // Logout from IMEI manager
                if (request.uri().equalsIgnoreCase("/imei_manager/logout")) {
                    request.session().removeAttribute(USER_ID_SESSION_KEY);
                    request.attribute(RequestContext.REQUEST_FIELD_ERROR_DTO, new ErrorDto(MessageKeys.ERR_ACCESS_DENIED));
                    return;
                }
                
                if (!isIpAllowedToAddImei) {
                    logger.error("Attempt to reach IMEI manager from unauthorized IP: " + request.ip());
                    request.attribute(RequestContext.REQUEST_FIELD_ERROR_DTO, new ErrorDto(MessageKeys.ERR_ACCESS_DENIED));
                    return;
                }
            }
            
            rc.setUser(user);
        } catch(ProviderException e) {
            request.attribute(RequestContext.REQUEST_FIELD_ERROR_DTO, new ErrorDto(MessageKeys.ERR_AUTH_NO_SUCH_USER));
            return;
        } catch(AuthenticationException e) {
            request.attribute(RequestContext.REQUEST_FIELD_ERROR_DTO, new ErrorDto(e.getMessage()));
            return;
        } catch(IllegalArgumentException e) {
            request.attribute(RequestContext.REQUEST_FIELD_ERROR_DTO, new ErrorDto(e.getMessage()));
            return;
        }
        // I got here that mean it's success
        request.attribute(RequestContext.REQUEST_FIELD_IS_AUTH, true);
    }

    public static boolean shouldPassErrorsToController(Request request) {
        return (request.pathInfo().matches("/v[0-9]+/users") 
                && request.requestMethod().equalsIgnoreCase("post"));
    }
    
    private static boolean shouldAllowUnauthorized(Request request) {
        return (request.pathInfo().matches("/v[0-9]+/users/activate/.*")
                && request.requestMethod().equalsIgnoreCase("get"))
                || (request.pathInfo().matches("/v[0-9]+/users/resetreq")
                && request.requestMethod().equalsIgnoreCase("post"))
                || (request.pathInfo().matches("/v[0-9]+/users/reset/.*")
                && request.requestMethod().equalsIgnoreCase("get"))
                || request.requestMethod().equalsIgnoreCase("options")
                || (request.pathInfo().matches("/v[0-9]+/users/resend")
                && request.requestMethod().equalsIgnoreCase("post"));
    }

    private User beginSession(Request request, UserProvider up) throws IllegalArgumentException, AuthenticationException {
        Credentials credentials = readCredentials(request.headers(AUTH_HEADER_NAME));
        User user = verifyCredentials(credentials, up);
        request.session().maxInactiveInterval(SESSION_MAX_INACTIVE_INTERVAL);
        request.session().attribute(USER_ID_SESSION_KEY, user.getId());
        return user;
    }
    
    private User continueSession(Request request, UserProvider up) throws Exception {
        long userId = request.session().attribute(USER_ID_SESSION_KEY);
        return up.authenticateUser(userId);
    }
    
    public User verifyCredentials(Credentials credentials, UserProvider up) 
            throws AuthenticationException {
        User user;
        if(credentials == null)
            throw new IllegalArgumentException(MessageKeys.ERR_AUTH_NO_CREDENTIALS);
        if(credentials.getPassword().isEmpty())
            throw new AuthenticationException(MessageKeys.ERR_AUTH_NO_PASSWORD);
        user = up.authenticateUser(credentials.getLogin(), credentials.getPassword());
        if(user == null)
            throw new AuthenticationException(MessageKeys.ERR_AUTH_NO_SUCH_USER);
        return user;
    }
    
    public Credentials readCredentials(String header) throws IllegalArgumentException {
        if(header == null)
            throw new AuthenticationException(MessageKeys.ERR_AUTH_NO_CREDENTIALS);
        
        String param = checkAndRemoveScheme(header);
        try {
            return decodeCredentials(param);
        }catch(IllegalArgumentException e) {
            throw new AuthenticationException(MessageKeys.ERR_AUTH_PARAMETER_NOT_BASE64);
        }
    }
    
    private String checkAndRemoveScheme(String authInfo) throws IllegalArgumentException {
        if(authInfo == null)
            throw new IllegalArgumentException(MessageKeys.ERR_AUTH_NO_CREDENTIALS);
        String[] authInfoParts = authInfo.split(AUTH_INFO_SEPARATOR);
        if(authInfoParts.length != 2 || authInfoParts[1].isEmpty())
            throw new AuthenticationException(MessageKeys.ERR_AUTH_INVALID_HEADER_FORMAT);
        if(!authInfoParts[0].equalsIgnoreCase(SCHEME))
            throw new AuthenticationException(MessageKeys.ERR_AUTH_INVALID_SCHEME);
        return authInfoParts[1];
    }
    
    private Credentials decodeCredentials(String credentials) {
        return Credentials.fromBasic(credentials, CHARSET);
    }

    public void unauthorized(Response response, ErrorDto error) {
        Gson gson = Context.getInstance().getGson();
        String message = gson.toJson(error);
        unauthorized(response, message);
    }
    
    private void unauthorized(Response response, String errorMessage) {
        if(req.pathInfo().matches("/v[0-9]+/session/user") 
                && req.requestMethod().equalsIgnoreCase("get"))
            response.header("WWW-Authenticate", "xBasic realm=\"traccar-api\"");
        else
            response.header("WWW-Authenticate", SCHEME + " " + "realm=\"" + REALM + "\"");
        closeConnections();
        Spark.halt(401, errorMessage);
    }
    
    private void notFound() {
        Spark.halt(HttpStatuses.NOT_FOUND);
    }
    
    private void serverError(Response response, String errorMessage) {
        logger.error(errorMessage);
        closeConnections();
        Spark.halt(500);
    }
    
    private void closeConnections() {
        RequestContext rc = req.attribute(Application.REQUEST_CONTEXT_KEY);
        if (req != null && rc != null) {
            try {
                rc.commitTransaction();
                if (rc.isRequestForMetadata(req)) {
                    rc.commitMetadataTransaction();
                }
                rc.close();
            } catch (Exception e) {
                logger.error("Resources cannot be closed: " + e.getMessage());
            }
        } else {
            logger.error("Request or RequestContext can not be find, so resources can't properly be closed.");
        }
    }
}
