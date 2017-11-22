/*
 *   Copyright (C) 2016  Datamatica (dev@datamatica.pl)
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

import java.util.ArrayList;
import java.util.Arrays;
import pl.datamatica.traccar.api.metadata.model.CommandType;
import pl.datamatica.traccar.api.metadata.model.DeviceModel;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Jan Usarek
 */
public class CommandTypeProviderTest {
    private final String IN_AND_OUT_COMMAND_FORMAT = "TIMER,{frequency},{frequencyStop}#";
    
    @Test
    public void getCommandFormatFromCommandListWithOneElement() {
        DeviceModel devModel = new DeviceModel();
        CommandType cmdTypeMock = getCmdTypeMock("positionPeriodic", IN_AND_OUT_COMMAND_FORMAT, true);

        devModel.setCommandTypes(Arrays.asList(cmdTypeMock));

        CommandTypeProvider provider = new CommandTypeProvider(devModel);
        assertEquals(IN_AND_OUT_COMMAND_FORMAT, provider.getTcpCommand("positionPeriodic"));
    }

    @Test
    public void getCommandFormatFromCommandListWithManyElements() {
        DeviceModel devModel = new DeviceModel();

        devModel.setCommandTypes(new ArrayList<CommandType>() {
            {
                add(getCmdTypeMock("autoAlarmArm", null, false));
                add(getCmdTypeMock("positionPeriodic", "", false));
                add(getCmdTypeMock("positionPeriodic", IN_AND_OUT_COMMAND_FORMAT, true));
                add(getCmdTypeMock("getParams", null, false));
                add(getCmdTypeMock("getStatus", "", false));
                add(getCmdTypeMock("setTimezone", "GMT,E,{timezone}#", true));
                add(getCmdTypeMock("positionSingle", "", true));
            }
        });

        CommandTypeProvider provider = new CommandTypeProvider(devModel);
        assertEquals(IN_AND_OUT_COMMAND_FORMAT, provider.getTcpCommand("positionPeriodic"));
    }
    
    @Test (expected = IllegalStateException.class)
    public void testCommandFormatFromCommandListWithManyTheSameTcpCommands() {
        DeviceModel devModel = new DeviceModel();

        devModel.setCommandTypes(new ArrayList<CommandType>() {
            {
                add(getCmdTypeMock("autoAlarmArm", null, false));
                add(getCmdTypeMock("positionPeriodic", "", true));
                add(getCmdTypeMock("positionPeriodic", IN_AND_OUT_COMMAND_FORMAT, true));
                add(getCmdTypeMock("getParams", null, false));
                add(getCmdTypeMock("getStatus", "", false));
                add(getCmdTypeMock("setTimezone", "GMT,E,{timezone}#", true));
                add(getCmdTypeMock("positionSingle", "", true));
            }
        });

        CommandTypeProvider provider = new CommandTypeProvider(devModel);
        assertEquals(IN_AND_OUT_COMMAND_FORMAT, provider.getTcpCommand("positionPeriodic"));
    }
    
    @Test
    public void getCommandFormatFromCommandListWithNullTcpCommand() {
        DeviceModel devModel = new DeviceModel();

        devModel.setCommandTypes(new ArrayList<CommandType>() {
            {
                add(getCmdTypeMock("autoAlarmArm", null, false));
                add(getCmdTypeMock("positionPeriodic", null, true));
                add(getCmdTypeMock("positionPeriodic", "", false));
                add(getCmdTypeMock("getParams", null, false));
            }
        });

        CommandTypeProvider provider = new CommandTypeProvider(devModel);
        assertEquals("", provider.getTcpCommand("positionPeriodic"));
    }

    private CommandType getCmdTypeMock(String commandName, String commandTCP, boolean isTCP) {
        CommandType cmdTypeMock = mock(CommandType.class);

        when(cmdTypeMock.getCommandName()).thenReturn(commandName);
        when(cmdTypeMock.getCommandTCP()).thenReturn(commandTCP);
        when(cmdTypeMock.isTCP()).thenReturn(isTCP);

        return cmdTypeMock;
    }

}
