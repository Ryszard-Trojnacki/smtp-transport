package pl.rtprog.smtptransport.services;

import org.apache.tapestry5.ioc.ObjectLocator;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;

import pl.rtprog.smtptransport.MessageHandlerImpl;

/**
 * 
 * @author Ryszard Trojnacki
 */
public class MessageHandlerFactoryImpl implements MessageHandlerFactory {
	private ObjectLocator loc;
	
	public MessageHandlerFactoryImpl(ObjectLocator loc) {
		this.loc=loc;
	}

	@Override
	public MessageHandler create(MessageContext ctx) {
		MessageHandlerImpl mh=loc.autobuild(MessageHandlerImpl.class);
		mh.init(ctx);
		return mh;
	}

}
