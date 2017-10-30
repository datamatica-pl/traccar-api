/*
 *  Copyright (C) 2017  Datamatica (dev@datamatica.pl)
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
package pl.datamatica.traccar.api.providers;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import pl.datamatica.traccar.api.Context;

/**
 *
 * @author ŁŁ
 */
public class MessageProvider extends ProviderBase {
    
    private Map<String, String> i18n;
    
    public MessageProvider(EntityManager em, String dir, String language) {
        super(em);
        File f = new File(dir, language+".json");
        Gson gson = Context.getInstance().getGson();
        i18n = Collections.EMPTY_MAP;
        try {
            i18n = gson.fromJson(new InputStreamReader(new FileInputStream(f), "utf-8"), 
                    new TypeToken<Map<String, String>>(){}.getType());
        } catch (FileNotFoundException | UnsupportedEncodingException | JsonIOException | JsonSyntaxException ex) {
            Logger.getLogger(MessageProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getMessage(String key) {
        if(i18n.containsKey(key))
            return i18n.get(key);
        return "@@@"+key+"@@@";
    }
}
