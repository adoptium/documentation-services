package net.adoptium.documentationservices;

import net.adoptium.documentationservices.services.UpdateDocumentationService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class OnStartupService {

    private static final Logger LOG = LoggerFactory.getLogger(OnStartupService.class);

    private final UpdateDocumentationService updateDocumentationService;

    private final int periodeInSec;

    @Inject
    public OnStartupService(final UpdateDocumentationService updateDocumentationService,
                            @ConfigProperty(name = "documentation.refresh.periodeInSec") int periodeInSec) {
        this.updateDocumentationService = updateDocumentationService;
        this.periodeInSec = periodeInSec;
    }

    /**
     * Find a good way to create a startup method for microprofile applications
     *
     * @param init
     */
    public void init(@Observes @Initialized(Singleton.class) Object init) {
        LOG.info("STARTUP CALL");
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(() -> {
            updateDocumentationService.updateDocumentationIfRequired();
        }, 0, periodeInSec, TimeUnit.SECONDS);
    }
}
