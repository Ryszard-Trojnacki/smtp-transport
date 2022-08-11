package pl.rtprog.smtptransport.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;

/**
 * Application logic service.
 *
 * @author Ryszard Trojnacki
 */
public class LogicService {
    private final static Logger log= LoggerFactory.getLogger(LogicService.class);

    /**
     * Method that registers incoming message from SMTP server/printer.
     * @param m received message
     */
    public void registerInput(MimeMessage m) {
        try {
            log.debug("Registering new message to: {}", Arrays.asList(m.getAllRecipients()));
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
