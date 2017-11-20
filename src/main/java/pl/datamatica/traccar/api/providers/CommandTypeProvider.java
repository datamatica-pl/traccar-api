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

import java.util.List;
import java.util.stream.Collectors;
import pl.datamatica.traccar.api.metadata.model.CommandType;
import pl.datamatica.traccar.api.metadata.model.DeviceModel;

/**
 *
 * @author Jan Usarek
 */
public class CommandTypeProvider {

    private final DeviceModel deviceModel;

    public CommandTypeProvider(DeviceModel deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getTcpCommand(String commandTypeName) {

        List<CommandType> cmdTypeList = deviceModel
                .getCommandTypes()
                .stream()
                .filter(c -> c.getCommandName().equalsIgnoreCase(commandTypeName) && c.isTCP())
                .collect(Collectors.toList());

        if (cmdTypeList.size() == 1) {
            return cmdTypeList.get(0).getCommandTCP();
        } else if (cmdTypeList.isEmpty()) {
            return null;
        } else {
            throw new IllegalStateException("DeviceModel has one than one command of the same type.");
        }

    }

}
