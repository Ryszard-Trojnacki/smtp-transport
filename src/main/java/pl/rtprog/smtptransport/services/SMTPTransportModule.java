package pl.rtprog.smtptransport.services;

import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.subethamail.smtp.MessageHandlerFactory;

public class SMTPTransportModule {
    public static void bind(ServiceBinder binder) {
    	binder.bind(MessageHandlerFactory.class, MessageHandlerFactoryImpl.class);
    	binder.bind(ConfigurationService.class);
    	binder.bind(JobService.class);
    }
    
	public static void contributeFactoryDefaults(MappedConfiguration<String, String> configuration) {
	}

}
