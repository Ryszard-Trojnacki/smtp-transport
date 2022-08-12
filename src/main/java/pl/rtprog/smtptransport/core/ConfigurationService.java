package pl.rtprog.smtptransport.core;

import java.io.File;
import java.io.IOException;

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
	
	private long modifyTime =0;
	private Configuration last=null;
	
	public Configuration getConfiguration() {
		File conf=new File("smtp-transport.xml");
		if(!conf.exists()) {
			log.error("Missing configuration file '{}'!", conf.getName());
			throw new IllegalStateException("Missing configuration file");
		}
		try {
			synchronized (lock) {
				if (last == null) {
					log.debug("Loading configuration from file: {}", conf.getAbsolutePath());
					last = Configuration.load(conf);
					modifyTime = conf.lastModified();
				} else if (conf.lastModified() != modifyTime) {
					log.debug("Reloading configuration from file: {}", conf.getAbsolutePath());
					last = Configuration.load(conf);    // reloading
					modifyTime = conf.lastModified();
				}
				return last;
			}
		}catch (IOException e) {
			log.warn("Exception while trying to load configuration!", e);
			if(last!=null) return last;
			throw new RuntimeException(e);
		}
	}
}
