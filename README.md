# SMTP Transport
Simple SMTP Server for transferring data to other SMTP server.
Used for transferring SMTP data from old/limited mail senders (for example Ricoh SP 311SFN printer) to new servers.

## Example configuration
```yaml
port: 10025
backgroundSenders: 2
smtp:
  host: smtp.other.server.com
  port: 587
  mode: TLS
  username: ryszard.trojnacki
  password: mySecretPassword
  overrideFrom: scan@mydomain.com
  overrideFromName: Scanner
seafile:
  server: seafile.server.com
  username: ryszard.trojnacki
  password: seafile-secret-password
```
* `port` - local listening port; optional, default 25
* `backgroundSenders` - count of background workers; optional, default 0 - works in foreground
* `smtp.host` - target SMTP server; required
* `smtp.port` - target SMTP server port; optional, default 25, 465 (if SSL), 587 (if TLS)
* `smtp.mode` - transmission encoding one of: NORMAL, SSL, TLS; optional, default NORMAL
* `smtp.username` - target SMTP account username; required
* `smtp.password` - target SMTP account password; required
* `smtp.overrideFrom` - from e-mail address to set for outgoing messages; optional, default from address from received message
* `smtp.overrideFromName` - from name e-mail address to set for outgoing messages; optional, default from name from received message

## Build & run
To build application just get a copy from GIT.
Install Java JDK 11 and run
```bash
./gradlew distTar
```
in `build/distrubutions` directory there will be generated archive with application.
Copy this archive to installation directory, unpack it and start with `smtp-transport` script.
```bash
cd path_to_app && ./smtp-transport &
```

