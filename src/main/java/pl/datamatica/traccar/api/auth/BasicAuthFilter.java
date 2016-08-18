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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.*;
import static pl.datamatica.traccar.api.auth.AuthenticationException.*;
import pl.datamatica.traccar.model.User;

public class BasicAuthFilter extends FilterImpl {
    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String SCHEME = "Basic";
    private static final String REALM = "traccar-api";
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final String AUTH_INFO_SEPARATOR = " ";
    private static final String CREDENTIALS_SEPARATOR = ":";
    public static final String USER_ID_SESSION_KEY = "userId";
    
    private static final int SESSION_MAX_INACTIVE_INTERVAL = (int)TimeUnit.SECONDS.convert(14, TimeUnit.DAYS);
    
    private static final Logger logger = LoggerFactory.getLogger(BasicAuthFilter.class);
    
    private final IPasswordValidator passwordValidator;
    
    public BasicAuthFilter(final String path, IPasswordValidator passwordValidator) {
        super(path, "*/*");
        
        this.passwordValidator = passwordValidator;
    }
        
    @Override
    public void handle(Request request, Response response) throws Exception { 
        if(request.session().attributes().contains(USER_ID_SESSION_KEY))
            return;
        try {
            Credentials credentials = readCredentials(request);
            User user = verifyCredentials(credentials);
            request.session().maxInactiveInterval(SESSION_MAX_INACTIVE_INTERVAL);
            request.session().attribute(USER_ID_SESSION_KEY, user.getId());
        } catch(AuthenticationException e) {
            unauthorized(response, e.getMessage());
        } catch(IllegalArgumentException e) {
            serverError(response, e.getMessage());
        }
    }
    
    public User verifyCredentials(Credentials credentials) throws AuthenticationException {
        User user;
        if(credentials == null)
            throw new AuthenticationException(ErrorType.NO_CREDENTIALS);
        else {
            user = passwordValidator.getUser(credentials);
            if(user == null)
                throw new AuthenticationException(ErrorType.NO_SUCH_USER);
            return user;
        }
    }
    
    public Credentials readCredentials(Request request) throws IllegalArgumentException {
        String header = request.headers(AUTH_HEADER_NAME);
        if(header == null)
            return null;
        
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
        if(authInfoParts.length != 2)
            throw new AuthenticationException(ErrorType.HEADER_FORMAT);
        if(!authInfoParts[0].equalsIgnoreCase(SCHEME))
            throw new AuthenticationException(ErrorType.INVALID_SCHEME);
        return authInfoParts[1];
    }
    
    private Credentials decodeCredentials(String credentials) {
        String decoded = new String(Base64.getDecoder().decode(credentials), CHARSET);
        int firstSeparator = decoded.indexOf(CREDENTIALS_SEPARATOR);
        if(firstSeparator == -1)
            throw new AuthenticationException(ErrorType.NO_PASSWORD);
        String login = decoded.substring(0, firstSeparator);
        String password = decoded.substring(firstSeparator + 1);
        return new Credentials(login, password);
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
