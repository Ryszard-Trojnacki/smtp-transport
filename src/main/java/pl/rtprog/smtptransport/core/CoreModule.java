package pl.rtprog.smtptransport.core;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class CoreModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ConfigurationService.class).in(Singleton.class);
        bind(JobService.class).in(Singleton.class);
    }
}
