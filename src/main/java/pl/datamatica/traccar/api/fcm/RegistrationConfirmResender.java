/*
 *  Copyright (C) 2018  Datamatica (dev@datamatica.pl)
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
package pl.datamatica.traccar.api.fcm;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.datamatica.traccar.api.providers.MailSender;
import pl.datamatica.traccar.model.User;

/**
 *
 * @author Jan Usarek
 */
public class RegistrationConfirmResender extends Thread {

    private Thread t;
    private final String threadName;
    private final String emailTitle;
    private final String emailMsg;
    private final Logger logger;
    private final String className;
    private final MailSender sender;
    private final User user;
    private final static int NUM_OF_TRIES = 3;
    private final static int SLEEP_TIME_SECONDS = 60;
    
    public RegistrationConfirmResender(MailSender sender, User user, String title, String msgContent) {
        this.sender = sender;
        this.user = user;
        emailTitle = title;
        emailMsg = msgContent;
        className = RegistrationConfirmResender.class.getName();
        threadName = className + "-" + user.getId();
        logger = Logger.getLogger(className);
    }

    public void run() {
        try {
            for (int i = 1; i <= NUM_OF_TRIES; i++) {
                Thread.sleep(TimeUnit.SECONDS.toMillis(SLEEP_TIME_SECONDS));
                logger.log(Level.INFO,
                        String.format("%s: is trying to resend confirmation for user with id %d. Try %d of %d.",
                                threadName, user.getId(), i, NUM_OF_TRIES));
                Boolean sendResult = sender.sendMessage(user.getEmail(), emailTitle, emailMsg);
                if (sendResult) {
                    logger.log(Level.INFO,
                            String.format("Confirmation successfully sent by %s to user with id %d.", threadName, user.getId()));
                    break;
                } else {
                    if (i == NUM_OF_TRIES) {
                        logger.log(Level.SEVERE,
                                String.format("Resending confirmation for user id %d failed.", user.getId()));
                    }
                }
            }
        } catch (InterruptedException ie) {
            logger.log(Level.SEVERE, String.format("%s: resending confirmation email interrupted: ", className), ie);
        }
        logger.log(Level.INFO, "Confirmation resender " + threadName + " exiting.");
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

}
