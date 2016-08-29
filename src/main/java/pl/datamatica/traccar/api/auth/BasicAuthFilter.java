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
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.Context;
import spark.*;
import static pl.datamatica.traccar.api.auth.AuthenticationException.*;
import pl.datamatica.traccar.api.controllers.RequestContext;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.providers.ApplicationSettingsProvider;
import pl.datamatica.traccar.api.providers.UserProvider;
import pl.datamatica.traccar.model.User;

public class BasicAuthFilter {
    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String SCHEME = "Basic";
    private static final String REALM = "traccar-api";
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final String AUTH_INFO_SEPARATOR = " ";
    public static final String USER_ID_SESSION_KEY = "userId";
    
    private static final int SESSION_MAX_INACTIVE_INTERVAL = (int)TimeUnit.SECONDS.convert(14, TimeUnit.DAYS);
    
    private static final Logger logger = LoggerFactory.getLogger(BasicAuthFilter.class);
        
    public void handle(Request request, Response response) throws Exception {
        if(request.pathInfo().equals("/v1/users") 
                && request.requestMethod().equalsIgnoreCase("post"))
            return;
        try {
            RequestContext rc = request.attribute(Application.REQUEST_CONTEXT_KEY);
            UserProvider up = rc.getUserProvider();
            ApplicationSettingsProvider asp = rc.getApplicationSettingsProvider();
            User user;
            if(request.session().attributes().contains(USER_ID_SESSION_KEY))
                user = continueSession(request, up);
            else
                user = beginSession(request, up);
            if(user.isBlocked()) {
                unauthorized(response, new ErrorDto(MessageKeys.ERR_ACCOUNT_BLOCKED));
            } else if(user.isExpired()) 
                unauthorized(response, new ErrorDto(MessageKeys.ERR_ACCOUNT_EXPIRED));
            rc.setUser(user);
        } catch(AuthenticationException e) {
            unauthorized(response, e.getMessage());
        } catch(IllegalArgumentException e) {
            serverError(response, e.getMessage());
        }
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
        User user = up.authenticateUser(userId);
        if(user == null)
            throw new AuthenticationException(ErrorType.NO_SUCH_USER);
        return user;
    }
    
    public User verifyCredentials(Credentials credentials, UserProvider up) 
            throws AuthenticationException {
        User user;
        if(credentials == null)
            throw new IllegalArgumentException("Credentials can't be null");
        if(credentials.getPassword().isEmpty())
            throw new AuthenticationException(ErrorType.NO_PASSWORD);
        user = up.authenticateUser(credentials.getLogin(), credentials.getPassword());
        if(user == null)
            throw new AuthenticationException(ErrorType.NO_SUCH_USER);
        return user;
    }
    
    public Credentials readCredentials(String header) throws IllegalArgumentException {
        if(header == null)
            throw new AuthenticationException(ErrorType.NO_CREDENTIALS);
        
        String param = checkAndRemoveScheme(header);
        try {
            return decodeCredentials(param);
        }catch(IllegalArgumentException e) {
            throw new AuthenticationException(ErrorType.PARAMETER_NOT_BASE64);
        }
    }
    
    private String checkAndRemoveScheme(String authInfo) throws IllegalArgumentException {
        if(authInfo == null)
            throw new IllegalArgumentException("authInfo can't be null");
        String[] authInfoParts = authInfo.split(AUTH_INFO_SEPARATOR);
        if(authInfoParts.length != 2 || authInfoParts[1].isEmpty())
            throw new AuthenticationException(ErrorType.HEADER_FORMAT);
        if(!authInfoParts[0].equalsIgnoreCase(SCHEME))
            throw new AuthenticationException(ErrorType.INVALID_SCHEME);
        return authInfoParts[1];
    }
    
    private Credentials decodeCredentials(String credentials) {
        return Credentials.fromBasic(credentials, CHARSET);
    }

    private void unauthorized(Response response, ErrorDto error) {
        Gson gson = Context.getInstance().getGson();
        String message = gson.toJson(error);
        unauthorized(response, message);
    }
    
    private void unauthorized(Response response, String errorMessage) {
        response.header("WWW-Authenticate", SCHEME + " " + "realm=\"" + REALM + "\"");
        Spark.halt(401, errorMessage);
    }
    
    private void serverError(Response response, String errorMessage) {
        logger.error(errorMessage);
        Spark.halt(500);
    }
}
