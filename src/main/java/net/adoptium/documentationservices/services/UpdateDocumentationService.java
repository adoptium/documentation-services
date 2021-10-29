package net.adoptium.documentationservices.services;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.logging.Logger;

@ApplicationScoped
public class UpdateDocumentationService {

    private Logger logger = Logger.getLogger(UpdateDocumentationService.class.getName());

    private final RepoService repoService;

    @Inject
    public UpdateDocumentationService(RepoService repoService) {
        this.repoService = repoService;
    }


    /**
     * Main method which updates documentation from the repo if required.
     */
    public void updateDocumentationIfRequired() {
        logger.info("Checking for update in documentation repository.");
        try {
            // check if there is something to do
            if (repoService.isUpdateAvailable()) {
                logger.info("Starting documentation update.");
                final Instant updateTimestamp = Instant.now();

                // download files from repo
                final Path repoContent = repoService.downloadRepositoryContent();

                // TODO - process files in repoContent - issue
                logger.info("Downloaded files can now be found in " + repoContent.toString());

                // save update timestamp
                repoService.saveLastUpdateTimestamp(updateTimestamp);
                logger.info("Finished documentation update.");
            }
        } catch (IOException e) {
            logger.severe("Encountered IOException while updating documentation: " + e.getMessage());
        }
    }
}
