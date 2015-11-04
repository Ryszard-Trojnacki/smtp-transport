# SMTP Transport
Simple SMTP Server for transferring data to other SMTP server.
Used for transferring SMTP data from old/limited mail senders (for example Ricoh SP 311SFN printer) to new servers.

# Example configuration
```xml
<configuration>
	<port>10025</port>
	<smtpHost>smtp.other.server.com</smtpHost>
	<smtpPort>587</smtpPort>
	<smtpEncryption>TLS</smtpEncryption>
	<smtpUsername>ryszard.trojnacki</smtpUsername>
	<smtpPassword>mySecretPassword</smtpPassword>
	<overrideFrom>scan@mydomain.com</overrideFrom>
	<overrideFromName>Scanner</overrideFromName>
	<backgroundSenders>2</backgroundSenders>
</configuration>
```

