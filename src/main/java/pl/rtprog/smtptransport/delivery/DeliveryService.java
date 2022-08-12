package pl.rtprog.smtptransport.delivery;

import javax.mail.Address;
import java.nio.file.Path;

/**
 * Interface for delivery services.
 *
 * @author Ryszard Trojnacki
 */
public interface DeliveryService {
    /**
     * Method for deliver data.
     * @param to recipient  to deliver data to.
     * @param name file name
     * @param data data (PDF file).
     */
    void deliver(Address to, String name, Path data);
}
