# SMTP Transport
Simple SMTP Server for transferring data to other SMTP server.
Used for transferring SMTP data from old/limited mail senders (for example Ricoh SP 311SFN printer) to new servers.

## Example configuration
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
* **port** - local listening port; optional, default 25
* **smtpHost** - target SMTP server; required
* **smtpPort** - target SMTP server port; optional, default 25, 465 (if SSL), 587 (if TLS)
* **smtpEncryption** - transmission encoding one of: NORMAL, SSL, TLS; optional, default NORMAL
* **smtpUsername** - target SMTP account username; required
* **smtpPassword** - target SMTP account password; required
* **overrideFrom** - from e-mail address to set for outgoing messages; optional, default from address from received message
* **overrideFromName** - from name e-mail address to set for outgoing messages; optional, default from name from received message
* **backgroundSenders** - count of background workers; optional, default 0 - works in foreground

