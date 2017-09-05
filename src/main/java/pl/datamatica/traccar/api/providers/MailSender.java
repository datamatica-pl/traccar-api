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
package pl.datamatica.traccar.api.providers;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import pl.datamatica.traccar.model.NotificationSettings;

public class MailSender {
    private NotificationSettings settings;
    
    public MailSender(EntityManager em) {
        //https://hibernate.atlassian.net/browse/HHH-5159
        // I think it IS legitimate bug in ORM 4
        List<NotificationSettings> allSettings = em.createQuery("SELECT s FROM NotificationSettings s "
                + "WHERE :serverManagement IN ELEMENTS(s.user.userGroup.permissions) "
                + "ORDER BY s.user.id ASC", 
                NotificationSettings.class)
                .setParameter("serverManagement", "SERVER_MANAGEMENT")
                .getResultList();
        
        if(allSettings.isEmpty())
            throw new IllegalStateException("NotificationSetting unavailable");
        settings = allSettings.get(0);
    }
    
    public boolean sendMessage(String address, String subject, String message) {
        Session session = getSession(settings);
        MimeMessage msg = new MimeMessage(session);
        Transport transport = null;
        try {
            msg.setFrom(new InternetAddress(settings.getFromAddress()));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(address, false));
            msg.setSubject(subject, "UTF-8");

            msg.setContent(message, "text/html; charset=utf-8");
            msg.setHeader("X-Mailer", "traccar-web.sendmail");
            msg.setSentDate(new Date());

            transport = session.getTransport("smtp");
            transport.connect();
            transport.sendMessage(msg, msg.getAllRecipients());

            return true;
        } catch (MessagingException me) {
            return false;
        } finally {
            if (transport != null) try { transport.close(); } catch (MessagingException ignored) {}
        }
    }
    
    private static Session getSession(NotificationSettings settings) {
        final boolean DEBUG = false;
        Properties props = new Properties();

        props.put("mail.smtp.host", settings.getServer());
        props.put("mail.smtp.auth", Boolean.toString(settings.isUseAuthorization()));
        props.put("mail.debug", Boolean.toString(DEBUG));
        props.put("mail.smtp.port", Integer.toString(settings.getPort()));

        switch (settings.getSecureConnectionType()) {
            case SSL_TLS:
                props.put("mail.smtp.socketFactory.port", Integer.toString(settings.getPort()));
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.fallback", "false");
                props.put("mail.smtp.socketFactory.timeout", 10 * 1000);
                break;
            case STARTTLS:
                props.put("mail.smtp.starttls.required", "true");
                break;
        }

        final String userName = settings.getUsername();
        final String password = settings.getPassword();

        Authenticator authenticator = settings.isUseAuthorization() ? new Authenticator()
        {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(userName, password);
            }
        } : null;

        Session s = Session.getInstance(props, authenticator);
        s.setDebug(DEBUG);

        return s;
    }
}
