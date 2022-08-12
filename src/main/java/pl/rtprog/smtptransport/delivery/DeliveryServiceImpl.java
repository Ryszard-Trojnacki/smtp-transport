package pl.rtprog.smtptransport.delivery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.rtprog.smtptransport.core.ConfigurationService;

import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.nio.file.Path;

public class DeliveryServiceImpl implements DeliveryService {
    private final static Logger log= LoggerFactory.getLogger(DeliveryServiceImpl.class);

    @Inject
    private ConfigurationService cs;

    private void seafileDelivery(Address to, String name, Path data) throws IOException, InterruptedException {
        var cfg=cs.getConfiguration().getSeafile();
        if(cfg==null) throw new IllegalStateException("Missing Seafile configuration!");
        log.debug("Delivering over seafile to {} file {}", to, name);

        var server=cfg.getServer();
        if(!server.startsWith("http://") && !server.startsWith("https://")) server="https://"+server;
        if(server.endsWith("/")) server=server.substring(0, server.length()-1);

        var sf=new SeafileClient(server);
        if(!sf.ping()) {
            log.warn("Seafile server not responding!");
            return;
        }
        if(!sf.auth(cfg.getUsername(), cfg.getPassword())) {
            log.warn("Invalid seafile authentication!");
        }
        var addr=(InternetAddress)to;
        var rep=sf.defaultRepo();
        var dir=sf.getDir(rep, addr.getAddress());
        if(dir==null) {
            log.debug("Missing target directory, creating");
        }
    }

    @Override
    public void deliver(Address to, String name, Path data) {
        try {
            seafileDelivery(to, name, data);
        }catch (Exception e) {
            log.error("Error delivering to: "+to, e);
        }
    }
}
