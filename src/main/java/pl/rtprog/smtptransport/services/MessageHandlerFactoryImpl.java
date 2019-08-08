package pl.rtprog.smtptransport.services;

import com.google.inject.Injector;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import pl.rtprog.smtptransport.MessageHandlerImpl;

import javax.inject.Inject;

/**
 * 
 * @author Ryszard Trojnacki
 */
public class MessageHandlerFactoryImpl implements MessageHandlerFactory {
	private Injector loc;

	@Inject
	public MessageHandlerFactoryImpl(Injector loc) {
		this.loc=loc;
	}

	@Override
	public MessageHandler create(MessageContext ctx) {
		MessageHandlerImpl mh=loc.getInstance(MessageHandlerImpl.class);
		mh.init(ctx);
		return mh;
	}

}
