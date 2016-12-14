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
package pl.datamatica.traccar.api.dtos.out;

import java.util.List;

/**
 *
 * @author Łukasz Ławniczak
 */
public class ListDto<T> {
    private final List<T> changed;
    private final long[] ids;
    private boolean hasMore;
    
    public ListDto(List<T> changed, int maxSize) {
        this.hasMore = changed.size() > maxSize;
        if(hasMore)
            this.changed = changed.subList(0, maxSize);
        else
            this.changed = changed;
        this.ids = null;
    }
    
    public ListDto(List<T> changed, long[] ids) {
        this.changed = changed;
        this.ids = ids;
    }

    public List<T> getChanged() {
        return changed;
    }

    public long[] getIds() {
        return ids;
    }
}
