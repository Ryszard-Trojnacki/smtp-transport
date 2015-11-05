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
	
	/** Number of background senders. 0 means send while receiving (blocks) */
	@XStreamAlias("backgroundSenders")
	private Integer backgroundThreads;
	
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
	
	/** From email */
	@XStreamAlias("overrideFrom")
	private String fromEmail;
	
	/** From name */
	@XStreamAlias("overrideFromName")
	private String fromName;
	
	/** Send Connection Timeout */
	@XStreamAlias("smtpTimeout")
	private Integer sendTimeout;
	
	
	public Integer getListenPort() {
		if(listenPort!=null) return listenPort;
		return 25;	// default value if not set 
	}
	
	public Integer getConnectionTimeout() {
		if(connectionTimeout!=null) return connectionTimeout;
		return 15;
	}
	
	public Integer getSendTimeout() {
		if(sendTimeout!=null) return sendTimeout;
		return 180;	// default 3 minutes
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

	public String getFromEmail() { return fromEmail; }
	public String getFromName() { return fromName; }
	
	public Integer getBackgroundThreads() { 
		if(backgroundThreads!=null) return backgroundThreads;
		return 0;	// default - not run in background
	}
	
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
