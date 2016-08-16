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

import java.util.Date;
import java.util.List;
import pl.datamatica.traccar.model.TimestampedEntity;
import spark.Spark;

public abstract class ControllerBase<T extends TimestampedEntity> {
    protected RequestContext requestContext;
    
    public abstract T get(long id);
    public abstract List<T> get();
    
    public ControllerBase(RequestContext requestContext) {
        this.requestContext = requestContext;
    }
    
    protected List<T> ok(List<T> list) {
        Date modificationTime = new Date(list.stream()
                .mapToLong(d -> d.getLastUpdate().getTime())
                .max()
                .orElse(0));
        
        requestContext.setLastModified(modificationTime);
        if(!requestContext.isModified()) {
            Spark.halt(304);
            return null;
        }
        
        return list;
    }
    
    protected T ok(T item) {
        requestContext.setLastModified(item.getLastUpdate());
        if(!requestContext.isModified()) {
            Spark.halt(304);
            return null;
        }
        return item;
    }
    
    protected T notFound() {
        Spark.halt(404);
        return null;
    }
    
    protected T forbidden() {
        Spark.halt(403);
        return null;
    }
}
