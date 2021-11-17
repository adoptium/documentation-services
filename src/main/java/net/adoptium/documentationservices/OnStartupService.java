package net.adoptium.documentationservices;

import net.adoptium.documentationservices.services.UpdateDocumentationService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
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
     * This constructor is needed to not end in the WELD-001410 issue:
     * "WELD-001410: The injection point has non-proxyable dependencies"
     * Seehttp://stackoverflow.com/questions/12291945/ddg#34375558
     */
    public OnStartupService() {
        this(null, -1);
    }

    /**
     * Find a good way to create a startup method for microprofile applications
     *
     * @param init
     */
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        LOG.info("STARTUP CALL");
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(() -> {
            updateDocumentationService.updateDocumentationIfRequired();
        }, 0, periodeInSec, TimeUnit.SECONDS);
    }
}
