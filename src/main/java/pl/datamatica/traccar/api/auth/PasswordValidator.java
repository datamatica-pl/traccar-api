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
import pl.datamatica.traccar.api.Context;
import pl.datamatica.traccar.model.User;

public class PasswordValidator implements IPasswordValidator {
    
    private static final boolean shouldHashPassword = true;
    
    private final EntityManager em;
    private final String salt;
    
    public PasswordValidator(Context context) {
        this(context.getEntityManager(), context.getSalt());
    }
    
    public PasswordValidator(EntityManager em, String salt) {
        this.em = em;
        this.salt = salt;
    }

    @Override
    public boolean validate(Credentials credentials) {
        try{
            User user = getUserByLogin(credentials.getLogin());
            String hashedPassword = getHashedPassword(user, credentials.getPassword());
            return hashedPassword.equals(user.getPassword());
        } catch(NoResultException e) {
            return false;
        }
    }

    private String getHashedPassword(User user, String password) {
        if(shouldHashPassword)
            return user.getPasswordHashMethod().doHash(password, salt);
        else
            return password;
    }
    
    public User getUserByLogin(String login) {
        TypedQuery<User> tq = em.createQuery("Select x from User x where x.login = :login", User.class);
        tq.setParameter("login", login);
        return tq.getSingleResult();
    }
}
