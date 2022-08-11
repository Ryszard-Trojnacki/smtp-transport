package pl.rtprog.smtptransport.delivery;

import javax.mail.Address;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface for delivery services.
 *
 * @author Ryszard Trojnacki
 */
public interface DeliveryService {
    /**
     * Method for deliver data.
     * @param to recipients to deliver data to; probably {@link Address}.
     * @param subject subject for data
     * @param data data (PDF file).
     */
    void deliver(List<Address> to, String subject, Path data);
}
