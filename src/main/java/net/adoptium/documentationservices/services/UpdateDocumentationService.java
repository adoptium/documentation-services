package net.adoptium.documentationservices.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Serializable;

@Singleton
public class UpdateDocumentationService implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateDocumentationService.class);

    private final RepoService repoService;

    @Inject
    public UpdateDocumentationService(RepoService repoService) {
        this.repoService = repoService;
    }

    /**
     * Main method which updates documentation from the repo if required.
     */
    public void updateDocumentationIfRequired() {
        LOG.info("Checking for update in documentation repository.");
        try {
            // check if there is something to do
            if (repoService.isUpdateAvailable()) {
                LOG.info("Starting documentation update.");
                // download files from repo
                repoService.downloadRepositoryContent();
                LOG.info("Finished documentation update.");
            }
        } catch (Exception e) {
            LOG.error("Encountered Exception while updating documentation.", e);
        }
    }
}
