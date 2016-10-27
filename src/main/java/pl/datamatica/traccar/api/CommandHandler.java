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
package pl.datamatica.traccar.api;

import java.util.Map;

public class CommandHandler implements ICommandHandler {
    private final Map<String,Object> result;
    private final Object awaiter;

    public CommandHandler(final Map<String,Object> result, final Object awaiter) {
        this.result = result;
        this.awaiter = awaiter;
    }

    @Override
    public void success(String data) {
        result.put("response", data);
        result.put("success", true);
        synchronized(awaiter) {
            awaiter.notifyAll();
        }
    }

    @Override
    public void fail(String reason) {
        result.put("success", false);
        result.put("reason", reason);
        synchronized(awaiter) {
            awaiter.notifyAll();
        }
    }
}
