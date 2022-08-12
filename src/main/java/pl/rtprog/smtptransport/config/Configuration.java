package pl.rtprog.smtptransport.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Root class of configuration structure (XML).
 * 
 * @author Ryszard Trojnacki
 */
public class Configuration {
	
	/** Number of background senders. 0 means send while receiving (blocks) */
	@JsonProperty("backgroundSenders")
	private Integer backgroundThreads;
	
	/** Listening port */
	@JsonProperty("port")
	private Integer listenPort;

	/** Target SMTP server configuration */
	private SMTPConfiguration smtp;

	private SeafileConfiguration seafile;

	public Integer getListenPort() {
		if(listenPort!=null) return listenPort;
		return 25;	// default value if not set 
	}

	public SMTPConfiguration getSmtp() {
		return smtp;
	}

	public SeafileConfiguration getSeafile() {
		return seafile;
	}

	public Integer getBackgroundThreads() {
		if(backgroundThreads!=null) return backgroundThreads;
		return 0;	// default - not run in background
	}
	
	private static ObjectMapper getObjectMapper() {
		var om=new ObjectMapper(new YAMLFactory());
		return om;
	}
	
	public static Configuration load(File f) throws IOException {
		return getObjectMapper().readValue(f, Configuration.class);
	}
	
	public static Configuration load(InputStream is) throws IOException {
		return getObjectMapper().readValue(is, Configuration.class);
	}
	
}
