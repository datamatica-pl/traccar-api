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
package pl.datamatica.traccar.api.auth;

import javax.persistence.*;
import pl.datamatica.traccar.api.providers.ApplicationSettingsProvider;
import pl.datamatica.traccar.api.providers.UserProvider;
import pl.datamatica.traccar.model.User;

public class PasswordValidator implements IPasswordValidator {
    
    private static final boolean shouldHashPassword = true;
    

    @Override
    public User getUser(Credentials credentials, UserProvider users,
            ApplicationSettingsProvider appSettings) {
        try {
            User user = users.getUserByMail(credentials.getLogin());
            String pass = credentials.getPassword();
            String salt = appSettings.get().getSalt();
            String hashedPassword = getHashedPassword(user, pass, salt);
            return hashedPassword.equals(user.getPassword()) ? user : null;
        } catch(NoResultException e) {
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private String getHashedPassword(User user, String password, String salt) {
        if(shouldHashPassword)
            return user.getPasswordHashMethod().doHash(password, salt);
        else
            return password;
    }
}
