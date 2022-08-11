package pl.rtprog.smtptransport.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of {@link MessageHandler} with Guice support.
 * This code is executed by {@link org.subethamail.smtp.server.SMTPServer} for each received email.
 * 
 * @author Ryszard Trojnacki
 */
public class MessageHandlerImpl implements MessageHandler {
	private final static Logger log=LoggerFactory.getLogger(MessageHandlerImpl.class);
	
	public void init(MessageContext ctx) {
		log.debug("Starting processing new message from: {}",ctx.getRemoteAddress());
	}

	@Override
	public void from(String from) throws RejectException {
		// TODO: Check from for validity
	}

	@Override
	public void recipient(String recipient) throws RejectException {
		// TODO: Check to for validity
	}

	@Override
	public void data(InputStream data) throws RejectException, IOException {
		// Read incoming message
		MimeMessage m;
		try {
			m=new MimeMessage(null, data);
			log.debug("Parsed incoming message ({} bytes) from: {}, to: {}",m.getSize(), m.getFrom(), m.getAllRecipients());
		} catch (MessagingException e) {
			log.warn("Error while reading incoming message with exception",e);
			throw new IOException("Error reading message with error: "+e.getMessage());
		}
	}

	@Override
	public void done() {

	}
}
