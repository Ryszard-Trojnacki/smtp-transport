package pl.rtprog.smtptransport.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SMTPConfiguration {
    /** Target SMTP Server */
    @JsonProperty("host")
    private String server;

    /** Target SMTP port */
    @JsonProperty("port")
    private Integer port;

    /** Target server transport type */
    @JsonProperty("mode")
    private SMTPEncryptionMode mode;

    /** Account username */
    @JsonProperty("username")
    private String username;

    /** Account password */
    @JsonProperty("password")
    private String password;

    /** Connection timeout in seconds */
    @JsonProperty("connectionTimeout")
    private Integer connectionTimeout;

    /** From email */
    @JsonProperty("from")
    private String fromEmail;

    /** From name */
    @JsonProperty("fromName")
    private String fromName;

    /** Send Connection Timeout */
    @JsonProperty("timeout")
    private Integer sendTimeout;

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

}
