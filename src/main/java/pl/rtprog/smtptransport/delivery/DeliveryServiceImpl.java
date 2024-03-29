package pl.rtprog.smtptransport.delivery;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.rtprog.smtptransport.core.ConfigurationService;

import javax.activation.FileDataSource;
import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class DeliveryServiceImpl implements DeliveryService {
    private final static Logger log= LoggerFactory.getLogger(DeliveryServiceImpl.class);

    @Inject
    private ConfigurationService cs;

    private boolean seafileDelivery(Address to, String name, Path data) throws IOException, InterruptedException, URISyntaxException {
        var cfg=cs.getConfiguration().getSeafile();
        if(cfg==null) throw new IllegalStateException("Missing Seafile configuration!");
        log.debug("Delivering over seafile to {} file {}", to, name);

        var server=cfg.getServer();
        if(!server.startsWith("http://") && !server.startsWith("https://")) server="https://"+server;
        if(server.endsWith("/")) server=server.substring(0, server.length()-1);

        var sf=new SeafileClient(server);
        if(!sf.ping()) {
            log.warn("Seafile server not responding!");
            return false;
        }
        if(!sf.auth(cfg.getUsername(), cfg.getPassword())) {
            log.warn("Invalid Seafile authentication!");
        }
        var addr=(InternetAddress)to;
        var repos=sf.listRepos();
        if(repos==null || repos.isEmpty()) {
            log.warn("Missing repositories in Seafile account");
            return false;
        }
        final var email=addr.getAddress();
        var target=repos.stream().filter(l -> email.equalsIgnoreCase(l.owner) || email.equalsIgnoreCase(l.ownerContactEmail)).findFirst();
        if(target.isEmpty()) {
            log.info("Missing repository for user: {}", addr.getAddress());
            return false;
        }
        log.debug("Uploading to repository: {}", target.get());
        var uploadId=sf.uploadFile(target.get().id, "/");
        if(StringUtils.isEmpty(uploadId)) {
            log.warn("Error uploading file");
            return false;
        }
        if(!sf.sendFile(uploadId, "/", data, name)) return false;

        return true;
    }

    private boolean mailDelivery(Address to, String name, Path data) throws IOException, EmailException, MessagingException {
        var cfg=cs.getConfiguration().getSmtp();
        if(cfg==null) throw new IllegalStateException();
        var addr=(InternetAddress)to;
        log.debug("Delivering over SMTP to {} file {}", to, name);

        var mailer=new MailClient();
        var mail=mailer.prepare();

        MimeMultipart msg=new MimeMultipart();
        var info=new MimeBodyPart();
        info.setText("Scan "+name, StandardCharsets.UTF_8.name());
        msg.addBodyPart(info);

        var attachment=new MimeBodyPart();
        attachment.setFileName(name);
        attachment.setContent(new FileDataSource(data.toFile()), "application/pdf");
        msg.addBodyPart(attachment);

        mail.addTo(addr.getAddress());
        mail.setSubject("Scan "+name);
        mail.setContent(msg);

        mailer.send(mail);

        return true;
    }

    @Override
    public void deliver(Address to, String name, Path data) {
        var cfg=cs.getConfiguration();
        if(cfg.getSeafile()!=null) {
            try {
                if(seafileDelivery(to, name, data)) return;
            } catch (Exception e) {
                log.error("Error delivering to: " + to, e);
            }
        }
        if(cfg.getSmtp()!=null) {
            try {
                if(mailDelivery(to, name, data)) return;
            }catch (Exception e) {
                log.error("Error delivering to: "+to, e);
            }
        }
    }
}
