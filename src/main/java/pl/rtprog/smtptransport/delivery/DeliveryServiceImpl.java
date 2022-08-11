package pl.rtprog.smtptransport.delivery;

import javax.mail.Address;
import java.nio.file.Path;
import java.util.List;

public class DeliveryServiceImpl implements DeliveryService {
    @Override
    public void deliver(List<Address> to, String subject, Path data) {

    }
}
