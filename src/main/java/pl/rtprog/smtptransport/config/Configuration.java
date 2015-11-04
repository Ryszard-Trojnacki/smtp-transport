package pl.rtprog.smtptransport.config;

import java.io.File;
import java.io.InputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Root class of configuration structure (XML).
 * 
 * @author Ryszard Trojnacki
 */
@XStreamAlias("configuration")
public class Configuration {
	
	/** Listening port */
	@XStreamAlias("port")
	private Integer listenPort;
	
	/** Target SMTP Server */
	@XStreamAlias("smtpHost")
	private String server;
	
	/** Target SMTP port */
	@XStreamAlias("smtpPort")
	private Integer port;
	
	/** Target server transport type */
	@XStreamAlias("smtpEncryption")
	private SMTPEncryptionMode mode;
	
	/** Account username */
	@XStreamAlias("smtpUsername")
	private String username;
	
	/** Account password */
	@XStreamAlias("smtpPassword")
	private String password;
	
	/** Connection timeout in seconds */
	@XStreamAlias("smtpConnectionTimeout")
	private Integer connectionTimeout;
	
	public Integer getListenPort() {
		if(listenPort!=null) return listenPort;
		return 25;	// default value if not set 
	}
	
	public Integer getConnectionTimeout() {
		if(connectionTimeout!=null) return connectionTimeout;
		return 15;
	}
	
	public String getServer() { return server; }
	
	public SMTPEncryptionMode getMode() { 
		return mode==null?SMTPEncryptionMode.NORMAL:mode;
	}
	
	/** Getter for smtp port with code for default value if not set */
	public int getPort() {
		if(port!=null) return port;
		switch(getMode()) {
		case NORMAL: return 25;
		case SSL: return 465;
		case TLS: return 587;
		}
		throw new IllegalStateException();
	}
	
	public String getUsername() { return username; }
	public String getPassword() { return password; }
	
	private static XStream getXStream() {
		XStream xs=new XStream(new DomDriver());
		xs.processAnnotations(Configuration.class);
		return xs;
	}
	
	public static Configuration load(File f) {
		return (Configuration)getXStream().fromXML(f);
	}
	
	public static Configuration load(InputStream is) {
		return (Configuration)getXStream().fromXML(is);
	}

}
