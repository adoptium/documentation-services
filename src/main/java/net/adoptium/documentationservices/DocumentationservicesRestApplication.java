package net.adoptium.documentationservices;

import net.adoptium.documentationservices.services.UpdateDocumentationService;

import javax.annotation.PostConstruct;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * JAX-RS application for documentation services.
 */
@ApplicationPath("/")
public class DocumentationservicesRestApplication extends Application {

    @Inject
    UpdateDocumentationService updateDocumentationService;

    @PostConstruct
    public void setupScheduledTask() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        // TODO - make interval configurable
        executorService.scheduleAtFixedRate(() -> {
            updateDocumentationService.updateDocumentationIfRequired();
        }, 30, 600, TimeUnit.SECONDS);

    }

}
