package net.adoptium.documentationservices;

import net.adoptium.documentationservices.services.RepoService;
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

    private final RepoService repoService;

    private final int periodeInSec;

    @Inject
    public OnStartupService(final RepoService repoService,
                            @ConfigProperty(name = "documentation.refresh.periodeInSec") final int periodeInSec) {
        this.repoService = repoService;
        this.periodeInSec = periodeInSec;
    }

    public void init(@Observes @Initialized(Singleton.class) Object init) {
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> updateRepo(), 0, periodeInSec, TimeUnit.SECONDS);
    }

    private void updateRepo() {
        try {
            if (repoService.isUpdateAvailable()) {
                repoService.downloadRepositoryContent();
            }
        } catch (final Exception e) {
            LOG.error("Error in repo update (check)", e);
        }
    }
}
