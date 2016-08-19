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
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.utils.DateUtil;
import pl.datamatica.traccar.model.User;
import spark.Request;
import spark.Response;

public class RequestContext {
    
    private static final String LAST_MODIFIED_HEADER = "Last-Modified";
    private static final String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";
    
    private Date ifModifiedSince;
    private Date lastModified;
    
    private final Response response;
    private final User user;
    private final EntityManager em;
    
    //only for testing!
    RequestContext(User user) {
        this.user = user;
        this.response = null;
        this.em = null;
    }
    
    public RequestContext(Request request, Response response) throws ParseException {
        this.ifModifiedSince = new Date(0);
        if(request.headers(IF_MODIFIED_SINCE_HEADER) != null)
            this.ifModifiedSince = DateUtil.parseDate(request.headers(IF_MODIFIED_SINCE_HEADER));
        this.response = response;
        this.user = request.attribute(Application.REQUEST_USER_KEY);
        this.em = request.attribute(Application.ENTITY_MANAGER_KEY);
    }
    
    public Date getModificationDate() {
        return ifModifiedSince;
    }
    
    public User getUser() {
        return user;
    }
    
    public EntityManager getEntityManager() {
        return em;
    }
}
