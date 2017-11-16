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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.metadata.model.CommandType;
import pl.datamatica.traccar.api.metadata.model.DeviceModel;

/**
 *
 * @author Jan Usarek
 */
public class StringCommandFormatter {

    private final DeviceModel deviceModel;
    private final String commandType;
    private final Map<String, Object> commandParams;
    private final List<String> supportedDevices = Arrays.asList("Teltonika FMB");

    // PreFormattedCmdBuilder
    
    // Add parser Here - implementation
    public StringCommandFormatter(DeviceModel model, String commandType, Map<String, Object> commandParams) {
        this.deviceModel = model;
        this.commandType = commandType;
        this.commandParams = commandParams;
    }
    
    public boolean isDeviceSupported() {
        return supportedDevices.contains(deviceModel.getModelName());
    }
    
    // TODO: Maybe add some kind of isCommandCupported, it will allow us to skip some commands used
    // in superStatus like getParams, getStatus. SuperStatus may work (because commandType is changed later, when command is send),
    // but we're not sure yet.
    
    public String getFormattedCommand() {
        List<CommandType> cmdTypeList = deviceModel
                .getCommandTypes()
                .stream()
                .filter(c -> c.getCommandName().equalsIgnoreCase(commandType) && c.isTCP()) // Winne jest brak isTCP() :). Chyba bedzie trzeba debugowac wewnatrz lambda
//                .filter(c -> c.getCommandName().equalsIgnoreCase(commandType))
                .collect(Collectors.toList());
        
        if (cmdTypeList.size() > 1) {
            throw new IllegalStateException("DeviceModel has one than one command of the same type.");
        }
        
        Logger logger = LoggerFactory.getLogger(Application.class);
        logger.error("deviceModel.modelName: " + deviceModel.getModelName());
        logger.error("Command types size: " + deviceModel.getCommandTypes().size());
        logger.error("Command type: " + commandType);
        logger.error("Command type list size: " + cmdTypeList.size());
        logger.error("Command params size: " + commandParams.size());
        
        final String cmdFormat = cmdTypeList.get(0).getCommandSMS(); // TODO: getCommandTCP. //IndexOutOfBoundException
        
        logger.error("command format: " + cmdFormat);

        CommandParser cmdParser = new SimpleCommandParser();
        final String parsedCommand = cmdParser.parse(cmdFormat, commandParams);
        logger.error("parsed command: " + parsedCommand);
        
        return parsedCommand;
                
    }
}
