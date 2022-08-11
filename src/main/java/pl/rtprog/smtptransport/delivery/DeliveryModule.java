package pl.rtprog.smtptransport.delivery;

import com.google.inject.AbstractModule;

import javax.inject.Singleton;

/**
 * Module definition of delivery services.
 *
 * @author Ryszard Trojnacki
 */
public class DeliveryModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(DeliveryService.class).to(DeliveryServiceImpl.class).in(Singleton.class);
    }
}
