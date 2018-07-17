/*
 *  Copyright (C) 2016  Datamatica (dev@datamatica.pl)
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final String className;
    private final MailSender sender;
    private final User user;
    private final static int NUM_OF_TRIES = 3;
    private final static int SLEEP_TIME_SECONDS = 30;
    private final static Logger LOGGER = LoggerFactory.getLogger(RegistrationConfirmResender.class);
    
    public RegistrationConfirmResender(MailSender sender, User user, String title, String msgContent) {
        this.sender = sender;
        this.user = user;
        emailTitle = title;
        emailMsg = msgContent;
        className = RegistrationConfirmResender.class.getName();
        threadName = className + "-" + user.getId();
    }

    public void run() {
        try {
            for (int i = 0; i < NUM_OF_TRIES; i++) {
                Thread.sleep(TimeUnit.SECONDS.toMillis(SLEEP_TIME_SECONDS));
                LOGGER.info(String.format("%s: is trying to resend confirmation. Try %d of %d", threadName, i + 1, NUM_OF_TRIES));
                Boolean sendResult = sender.sendMessage(user.getEmail(), emailTitle, emailMsg);
                if (sendResult) {
                    LOGGER.info("Confirmation successfully sent by " + threadName);
                    break;
                }
            }
        } catch (InterruptedException ie) {
            LOGGER.error(String.format("%s: resending confirmation email interrupted: ", className), ie);
        }
        LOGGER.info("Confirmation resender " + threadName + " exiting.");
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

}
