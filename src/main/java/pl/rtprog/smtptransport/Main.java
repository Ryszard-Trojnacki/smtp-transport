package pl.rtprog.smtptransport;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.server.SMTPServer;

/**
 * Startup class.
 * 
 * @author Ryszard Trojnacki
 */
public class Main {
	public static void main(String[] args) {
		SMTPServer smtpd=new SMTPServer(new MessageHandlerFactory() {
			@Override
			public MessageHandler create(MessageContext ctx) {
				return null;
			}
		});
	}
}
