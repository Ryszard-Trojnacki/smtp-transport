package pl.rtprog.smtptransport;

import org.apache.log4j.AsyncAppender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.server.SMTPServer;

import pl.rtprog.smtptransport.config.Configuration;
import pl.rtprog.smtptransport.services.ConfigurationService;
import pl.rtprog.smtptransport.services.SMTPTransportModule;

/**
 * Startup class.
 * 
 * @author Ryszard Trojnacki
 */
public class Main implements Runnable {
	private static final Logger log=LoggerFactory.getLogger(Main.class);
	
	private SMTPServer server;
	
	@Inject
	private MessageHandlerFactory mhf;
	
	@Inject
	private ConfigurationService cs;
	
	@PostInjection
	public void init() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutdown();
			}
		});
	}
	
	private void shutdown() {
		log.debug("Shutting down");
		if(server!=null) {
			server.stop();
		}
	}
	
	@Override
	public void run() {
		log.info("Application started");
		server=new SMTPServer(mhf);
		Configuration cfg=cs.getConfiguration();
		server.setPort(cfg.getListenPort());		// TODO: Add change monitoring
		server.start();
		while(server.isRunning()) {
			synchronized(this) {
				try {
					wait(5);
				} catch (InterruptedException e) {
					log.warn("Unexpected interrupt exception.",e);
					break;
				}
			}
		}
	}
	
	public static void main(String[] args) {
		System.setProperty("tapestry.service-reloading-enabled","false");
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				log.error("Got uncaught exception from thread {}: {}",t,e);
			}
		});
		if("true".equalsIgnoreCase(System.getProperty("debug"))) {	// if in debug mode then log to stdout
			ConsoleAppender ca=new ConsoleAppender();
			ca.setName("console");
			ca.setLayout(new PatternLayout("%d (%X{context}) [%5p] %c | %m%n"));
			ca.activateOptions();
			((AsyncAppender)org.apache.log4j.Logger.getRootLogger().getAppender("async")).removeAllAppenders();
			((AsyncAppender)org.apache.log4j.Logger.getRootLogger().getAppender("async")).addAppender(ca);
		}
		SLF4JBridgeHandler.install();

		// build registry
		RegistryBuilder rb=new RegistryBuilder();
		rb.add(SMTPTransportModule.class);
		Registry reg=rb.build();
		try {
			// create main class
			Main m=reg.autobuild(Main.class);
			m.run();	// and start worker
		}catch(Throwable e) {
			log.error("Unexpected exception",e);
		}finally {
			reg.shutdown();
		}
	}
}
