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
package pl.datamatica.traccar.api.dtos.out;

import java.util.Objects;

public class ErrorDto {
    private final String messageKey;
    private final String localizedMessage;
    
    public ErrorDto(String messageKey) {
        this(messageKey, null);
    }
    
    public ErrorDto(String messageKey, String localizedMessage) {
        this.messageKey = messageKey;
        this.localizedMessage = localizedMessage;
    }
    
    public String getMessageKey() {
        return messageKey;
    }

    public String getLocalizedMessage() {
        return localizedMessage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.messageKey, this.localizedMessage);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ErrorDto other = (ErrorDto) obj;
        return Objects.equals(this.localizedMessage, other.localizedMessage)
                && Objects.equals(this.messageKey, other.messageKey);
    }
    
    
}
