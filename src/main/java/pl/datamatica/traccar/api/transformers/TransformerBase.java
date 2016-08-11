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
package pl.datamatica.traccar.api.transformers;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import spark.ResponseTransformer;

public abstract class TransformerBase<In> implements ResponseTransformer {
    
    private final Gson gson;
    private final Class<?> in;
    
    public TransformerBase(Gson gson, Class<?> in) {
        this.gson = gson;
        this.in = in;
    }
    
    @Override
    public String render(Object o) throws Exception {
        if(o instanceof Iterable) {
            List result = new ArrayList<>();
            for(Object element : (Iterable)o) {
                if(in.isInstance(element))
                    result.add(transform((In)element));
                else
                    return null;
            }
            return gson.toJson(result);
        } else if(in.isInstance(o)) {
            return gson.toJson(transform((In)o));
        } else {
            return null;
        }
    }

    public abstract Object transform(In element);    
}
