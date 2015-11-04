package pl.rtprog.smtptransport.services;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rtprog.smtptransport.config.Configuration;

/**
 * Service that does configuration loading and reloading...
 * 
 * @author Ryszard Trojnacki
 */
public class ConfigurationService {
	private final static Logger log=LoggerFactory.getLogger(ConfigurationService.class);
	
	private final Object lock=new Object();
	
	private long modifiTime=0;
	private Configuration last=null;
	
	public Configuration getConfiguration() {
		File conf=new File("smtp-transport.xml");
		synchronized(lock) {
			if(last==null) {
				log.debug("Loading configuration from file: {}",conf.getAbsolutePath());
				last=Configuration.load(conf);
				modifiTime=conf.lastModified();
			} else if(conf.lastModified()!=modifiTime) {
				log.debug("Reloading configuration from file: {}",conf.getAbsolutePath());
				last=Configuration.load(conf);	// reloading
				modifiTime=conf.lastModified();
			}
			return last; 
		}
	}
}
