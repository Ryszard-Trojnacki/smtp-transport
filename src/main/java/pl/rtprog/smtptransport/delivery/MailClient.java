package pl.rtprog.smtptransport.delivery;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.rtprog.smtptransport.core.ConfigurationService;
import pl.rtprog.smtptransport.core.JobService;

import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;

/**
 * Service for sending e-mail via SMTP server.
 *
 * @author Ryszard Trojnacki
 */
class MailClient {
    private final static Logger log= LoggerFactory.getLogger(MailClient.class);

    @Inject
    private ConfigurationService cs;
    @Inject
    private JobService js;

    public SimpleEmail prepare() {
        var email = new SimpleEmail();
        var cfg = cs.getConfiguration().getSmtp();
        if(cfg==null) throw new IllegalStateException("Missing SMTP configuration");

        int port = cfg.getPort();
        email.setDebug(false);    // TODO: Parameter for testing purposes
        email.setHostName(cfg.getServer());
        email.setAuthenticator(new DefaultAuthenticator(cfg.getUsername(), cfg.getPassword()));
        email.setSmtpPort(port);
        email.setSslSmtpPort(String.valueOf(port));
        email.setSSLCheckServerIdentity(false);
        email.setSocketConnectionTimeout(cfg.getConnectionTimeout() * 1000);
        email.setSocketTimeout(cfg.getSendTimeout() * 1000);
        switch (cfg.getMode()) {
            case NORMAL:
                break;
            case TLS:
                email.setStartTLSEnabled(true);
                break;
            case SSL:
                email.setSSLOnConnect(true);
                break;
        }
        return email;
    }

    public void transfer(MimeMessage m) throws IOException{
        var email = prepare();
        var cfg = cs.getConfiguration().getSmtp();
        if(cfg==null) throw new IllegalStateException("Missing SMTP configuration");

        // Set e-mail attributes from incoming message
        try {
            if (m.getRecipients(Message.RecipientType.TO) != null) {    // is that even possible?
                for (Address i : m.getRecipients(Message.RecipientType.TO)) {
                    InternetAddress ia = (InternetAddress) i;
                    if (ia.getPersonal() != null) email.addTo(ia.getAddress(), ia.getPersonal());
                    else email.addTo(ia.getAddress());
                }
            }
            if (m.getRecipients(Message.RecipientType.CC) != null) {
                for (Address i : m.getRecipients(Message.RecipientType.CC)) {
                    InternetAddress ia = (InternetAddress) i;
                    if (ia.getPersonal() != null) email.addCc(ia.getAddress(), ia.getPersonal());
                    else email.addCc(ia.getAddress());
                }
            }
            if (m.getRecipients(Message.RecipientType.BCC) != null) {
                for (Address i : m.getRecipients(Message.RecipientType.BCC)) {
                    InternetAddress ia = (InternetAddress) i;
                    if (ia.getPersonal() != null) email.addBcc(ia.getAddress(), ia.getPersonal());
                    else email.addBcc(ia.getAddress());
                }
            }
            if (cfg.getFromEmail() != null) {
                log.debug("Overriding from address");
                if (cfg.getFromName() != null) {
                    email.setFrom(cfg.getFromEmail(), cfg.getFromName());
                } else {
                    email.setFrom(cfg.getFromEmail());
                }
            } else {
                InternetAddress from = (InternetAddress) m.getFrom()[0];
                if (from.getPersonal() != null) {
                    email.setFrom(from.getAddress(), from.getPersonal());
                } else {
                    email.setFrom(from.getAddress());
                }
            }
            email.setSubject(m.getSubject());
            email.setContent(m.getContent(), m.getContentType());
            email.setCharset("UTF-8");
        } catch (MessagingException | EmailException e) {
            log.warn("Error while setting up email from received message", e);
            throw new IOException("Error processing message with error: " + e.getMessage(), e);
        }

        try {
            if(js.isBackgroundMode()) {
                log.debug("Sending message in background mode");
                js.run(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            log.debug("Background mode sending started");
                            String msgId=email.send();
                            log.info("Email send with id: {}", msgId);
                        } catch (EmailException e) {
                            log.error("Error while sending email",e);
                        }
                    }
                });
            } else {
                log.debug("Sending message in foreground (blocking mode)");
                email.send();
                log.info("Email send");
            }
        } catch (EmailException e) {
            log.error("Error while sending email",e);
            throw new IOException("Error while transferring message to target host with message: "+e.getMessage());
        }


    }
}
