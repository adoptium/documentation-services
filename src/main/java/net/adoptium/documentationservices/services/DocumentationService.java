package net.adoptium.documentationservices.services;

import net.adoptium.documentationservices.adoc.AsciiDocService;
import net.adoptium.documentationservices.model.Document;
import net.adoptium.documentationservices.model.Documentation;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class DocumentationService {

    private final RepoService repoService;

    private final AsciiDocService asciiDocService;

    @Inject
    public DocumentationService(final RepoService repoService, final AsciiDocService asciiDocService) {
        this.repoService = repoService;
        this.asciiDocService = asciiDocService;
    }

    @PostConstruct
    public void init() {
        try {
            if (repoService.isUpdateAvailable()) {
                repoService.downloadRepositoryContent();
            }
        } catch (final Exception e) {
            throw new IllegalStateException("Error in init of service", e);
        }
    }

    public Documentation getDocumentation(final String documentationId) throws IOException {
        final Path docPath = repoService.getLocalRepoPath().resolve(documentationId);
        final Set<Document> docs = Files.list(docPath)
                .filter(file -> file.toFile().getName().endsWith(".adoc"))
                .filter(file -> file.toFile().getName().startsWith("index_"))
                .map(file -> {
                    final String tile = asciiDocService.getTitle(file);
                    final String isoCode = file.toFile().getName().substring(6, file.toFile().getName().length() - 5);
                    final String htmlContent = asciiDocService.convertToHtmlContent(file);
                    return new Document(tile, isoCode, htmlContent);
                })
                .collect(Collectors.toSet());

        final Path defaultFile = docPath.resolve("index.adoc");
        final String htmlContent = asciiDocService.convertToHtmlContent(defaultFile);
        Document defaultDoc = new Document(asciiDocService.getTitle(defaultFile), "en", htmlContent);


        final Set<Document> allDocs = new HashSet<>(docs);
        allDocs.add(defaultDoc);
        return new Documentation(documentationId, allDocs);
    }

}
