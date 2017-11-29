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

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ŁŁ
 */
public class CommandParserTest {
    private IDeviceCommandParser cmdParser;
    
    @Before
    public void testInit() {
        cmdParser = new CommandParser();
    }
    
    @Test
    public void testWithoutParameters() {
        final Map<String, Object> commandParams = new HashMap<>();

        assertEquals("GPRSET#",
                cmdParser.parse("GPRSET#", commandParams));
    }
    
    @Test
    public void testWithOneParameter() {
        final Map<String, Object> commandParams = new HashMap<String, Object>() {
            {
                put("frequency", "20");
            }
        };

        assertEquals("run,20",
                cmdParser.parse("run,{frequency}", commandParams));
    }
    
    @Test
    public void testWithTwoParameters() {
        final Map<String, Object> commandParams = new HashMap<String, Object>() {
            {
                put("frequency", "30");
                put("frequencyStop", "300");
            }
        };

        assertEquals("TIMER,30,300#",
                cmdParser.parse("TIMER,{frequency},{frequencyStop}#", commandParams));
    }
    
    @Test
    public void testWithTooManyParameters() {
        final Map<String, Object> commandParams = new HashMap<String, Object>() {
            {
                put("frequency", "20");
                put("frequencyStop", "600");
                put("password", "111111");
                put("number", "499001002");
            }
        };

        assertEquals("TIMER,20,600#",
                cmdParser.parse("TIMER,{frequency},{frequencyStop}#", commandParams));
    }
}
