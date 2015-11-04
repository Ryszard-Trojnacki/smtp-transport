package pl.rtprog.smtptransport.services;

import java.io.File;

import pl.rtprog.smtptransport.config.Configuration;

/**
 * Service that does configuration loading and reloading...
 * 
 * @author Ryszard Trojnacki
 */
public class ConfigurationService {
	public Configuration getConfiguration() {
		// TODO: Implement storing of configuration and checking for changes
		return Configuration.load(new File("smtp-transport.xml"));
	}
}
