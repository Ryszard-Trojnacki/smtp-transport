package pl.rtprog.smtptransport.services;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.subethamail.smtp.MessageHandlerFactory;

public class SMTPTransportModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(MessageHandlerFactory.class).to(MessageHandlerFactoryImpl.class).in(Singleton.class);
		bind(ConfigurationService.class).in(Singleton.class);
		bind(JobService.class).in(Singleton.class);
	}

}
