/*
 *  Copyright (C) 2016  Datamatica (dev@datamatica.pl)
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
package pl.datamatica.traccar.api.services;

import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author ŁŁ
 */
public class CommandParser implements IDeviceCommandParser {

    @Override
    public String parse(String commandFormat, Map<String, Object> commandParams) {
        StringBuilder msg = new StringBuilder();
        Scanner sc = new Scanner(commandFormat);
        while(true) {
            if(sc.findWithinHorizon("\\{", 1) == null) {
                sc.useDelimiter("\\{");
                if(!sc.hasNext())
                    break;
                msg.append(sc.next());
                if(!sc.hasNext())
                    break;
                sc.skip("\\{");
            }
            sc.useDelimiter("\\}");
            String param = emplaceParam(commandParams, sc.next());
            if(param == null)
                return null;
            msg.append(param);
            sc.skip("\\}");
        }
        return msg.toString();
    }
    
    private String emplaceParam(Map<String, Object> params, String param) {
        if(param.contains(","))
            return transformParam(params, param.split(","));
        else if(params.containsKey(param))
            return params.get(param).toString();
        throw new UnsupportedOperationException();
    }
    
    private String transformParam(Map<String, Object> params, String[] t) {
        if(params.get(t[0]) == null)
            throw new UnsupportedOperationException();
        int v = Integer.parseInt(params.get(t[0]).toString());
        int p2 = Integer.parseInt(t[1]);
        switch(t[2].charAt(0)) {
            case '+':
                return Integer.toString(v+p2);
            case '-':
                return Integer.toString(v-p2);
            case '*':
                return Integer.toString(v*p2);
            case '/':
                return Integer.toString(v/p2);
        }
        throw new UnsupportedOperationException();
    }
    
}
