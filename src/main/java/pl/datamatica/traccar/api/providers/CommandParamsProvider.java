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

import java.util.HashMap;
import java.util.Map;
import pl.datamatica.traccar.api.services.IDeviceCommandParser;
import pl.datamatica.traccar.api.utils.JsonUtils;
import pl.datamatica.traccar.model.User;

/**
 *
 * @author Jan Usarek
 */
public class CommandParamsProvider {

    final IDeviceCommandParser cmdParser;
    final User user;

    public CommandParamsProvider(IDeviceCommandParser cmdParser, User user) {
        this.cmdParser = cmdParser;
        this.user = user;
    }

    public final Map<String, Object> getCommandParams(final String params, final String commandFormat) {
        final Map<String, Object> originalCmdParams = JsonUtils.getCommandParams(params);
        Map<String, Object> commandParams = new HashMap<>();

        commandParams.put("userId", user.getId());

        if (commandFormat.isEmpty()) {
            commandParams.putAll(originalCmdParams);

            if (commandParams.get("timezone") != null) {
                final long timezoneHours = Long.valueOf(commandParams.get("timezone").toString());
                final long timezoneSeconds = timezoneHours * 3600;
                commandParams.replace("timezone", timezoneSeconds);
            }
        } else {
            commandParams.put("message", cmdParser.parse(commandFormat, originalCmdParams));
        }

        return commandParams;
    }

}
