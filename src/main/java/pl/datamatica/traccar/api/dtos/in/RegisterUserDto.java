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
package pl.datamatica.traccar.api.dtos.in;

public class RegisterUserDto {
    private String email;
    private String password;
    private boolean checkMarketing;
    private String imei;
    
    public RegisterUserDto(String email, String password, boolean checkMarketing, String imei) {
        this.email = email;
        this.password = password;
        this.checkMarketing = checkMarketing;
        this.imei = imei;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isCheckMarketing() {
        return checkMarketing;
    }
    
    public String getImei() {
        return imei;
    }
}
