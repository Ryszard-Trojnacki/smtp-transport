package pl.rtprog.smtptransport;

import java.io.IOException;
import java.io.InputStream;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

import pl.rtprog.smtptransport.config.Configuration;
import pl.rtprog.smtptransport.services.ConfigurationService;
import pl.rtprog.smtptransport.services.JobService;

/**
 * Implementation of {@link MessageHandler} with Tapestry support.
 * 
 * @author Ryszard Trojnacki
 */
public class MessageHandlerImpl implements MessageHandler {
	private final static Logger log=LoggerFactory.getLogger(MessageHandlerImpl.class);
	
	@Inject
	private PerthreadManager tm;
	@Inject
	private ConfigurationService cs;
	@Inject
	private JobService js;
	
//	private MessageContext ctx;
	
	private Email email;

	public void init(MessageContext ctx) {
//		this.ctx=ctx;
		this.email=new SimpleEmail();
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
	public void data(InputStream data) throws RejectException, TooMuchDataException, IOException {
		// Set configuration attributes
		Configuration cfg=cs.getConfiguration();

		int port=cfg.getPort();
		email.setDebug(false);	// TODO: Parameter for testing purposes
		email.setHostName(cfg.getServer());
		email.setAuthenticator(new DefaultAuthenticator(cfg.getUsername(), cfg.getPassword()));
		email.setSmtpPort(port);
		email.setSslSmtpPort(String.valueOf(port));
		email.setSSLCheckServerIdentity(false);
		email.setSocketConnectionTimeout(cfg.getConnectionTimeout()*1000);
		email.setSocketTimeout(cfg.getSendTimeout()*1000);
		switch(cfg.getMode()) {
		case NORMAL:
			break;
		case TLS:
			email.setStartTLSEnabled(true);
			break;
		case SSL:
			email.setSSLOnConnect(true);
			break;
		}
		
		// Read incoming message
		MimeMessage m;
		try {
			m=new MimeMessage(email.getMailSession(), data);
			log.debug("Parsed incomming message ({} bytes) from: {}, to: {}",m.getSize(), m.getFrom(), m.getAllRecipients());
		} catch (MessagingException | EmailException e) {
			log.warn("Error while reading incoming message with exception",e);
			throw new IOException("Error reading message with error: "+e.getMessage());
		}
		// Set e-mail attributes from incoming message
		try {
			if(m.getRecipients(RecipientType.TO)!=null) {	// is that even possible?
				for(Address i : m.getRecipients(RecipientType.TO)) {
					InternetAddress ia=(InternetAddress)i;
					if(ia.getPersonal()!=null) email.addTo(ia.getAddress(), ia.getPersonal());
					else email.addTo(ia.getAddress());
				}
			}
			if(m.getRecipients(RecipientType.CC)!=null) {
				for(Address i : m.getRecipients(RecipientType.CC)) {
					InternetAddress ia=(InternetAddress)i;
					if(ia.getPersonal()!=null) email.addCc(ia.getAddress(), ia.getPersonal());
					else email.addCc(ia.getAddress());
				}
			}
			if(m.getRecipients(RecipientType.BCC)!=null) {
				for(Address i : m.getRecipients(RecipientType.BCC)) {
					InternetAddress ia=(InternetAddress)i;
					if(ia.getPersonal()!=null) email.addBcc(ia.getAddress(), ia.getPersonal());
					else email.addBcc(ia.getAddress());
				}
			}
			if(cfg.getFromEmail()!=null) {
				log.debug("Overriding from address");
				if(cfg.getFromName()!=null) {
					email.setFrom(cfg.getFromEmail(), cfg.getFromName());
				} else {
					email.setFrom(cfg.getFromEmail());
				}
			} else {
				InternetAddress from=(InternetAddress)m.getFrom()[0];
				if(from.getPersonal()!=null) {
					email.setFrom(from.getAddress(), from.getPersonal());
				} else {
					email.setFrom(from.getAddress());
				}
			}
			email.setSubject(m.getSubject());
			email.setContent(m.getContent(), m.getContentType());
			email.setCharset("UTF-8");
		}catch(MessagingException | EmailException e) {
			log.warn("Error while settingup email from received message",e);
			throw new IOException("Error processing message with error: "+e.getMessage());
		}
		
		try {
			if(js.isBackgroundMode()) {
				log.debug("Sending message in background mode");
				js.run(new Runnable() {
					@Override
					public void run() {
						try {
							log.debug("Background mode sending started");
							email.send();
							log.info("Email send");
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
			throw new IOException("Error while transferting message to target host with message: "+e.getMessage());
		}
	}

	@Override
	public void done() {
		tm.cleanup();
	}
}
