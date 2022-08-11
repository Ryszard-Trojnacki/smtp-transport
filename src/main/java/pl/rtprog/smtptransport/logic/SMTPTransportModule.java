package pl.rtprog.smtptransport.logic;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.subethamail.smtp.MessageHandlerFactory;

public class SMTPTransportModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(MessageHandlerFactory.class).to(MessageHandlerFactoryImpl.class).in(Singleton.class);
		bind(LogicService.class).in(Singleton.class);
	}

}
