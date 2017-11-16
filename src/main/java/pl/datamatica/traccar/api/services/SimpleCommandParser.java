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
package pl.datamatica.traccar.api.services;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.datamatica.traccar.api.Application;

/**
 *
 * @author Jan Usarek
 */
public class SimpleCommandParser implements CommandParser {
    
    @Override
    public String parse(String cmdFormat, Map<String, Object> commandParams) {
        Logger logger = LoggerFactory.getLogger(Application.class);
        
//        String cmdFormat = "setparam 1234 {timezone}";
        
        for (Map.Entry<String, Object> cmdParam : commandParams.entrySet()) {
            
            logger.error("cmdParam Key: " + cmdParam.getKey());
            logger.error("cmdParam Value: " + cmdParam.getValue());
            
            // TODO: Skip userId
            String paramToReplace = String.format("{%s}", cmdParam.getKey());
            cmdFormat = cmdFormat.replace(paramToReplace, cmdParam.getValue().toString());
        }
        
        return cmdFormat;
    }
    
}
