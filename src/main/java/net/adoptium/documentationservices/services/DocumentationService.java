package net.adoptium.documentationservices.services;

import net.adoptium.documentationservices.adoc.AsciiDocService;
import net.adoptium.documentationservices.model.Contributor;
import net.adoptium.documentationservices.model.Document;
import net.adoptium.documentationservices.model.Documentation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class DocumentationService {

    public static final String ADOC_FILETYPE = ".adoc";
    public static final String INDEX_FILE_PREFIX = "index_";
    public static final String DEFAULT_INDEX_ADOC_NAME = "index.adoc";
    public static final String EN_ISO_CODE = "en";

    private final RepoService repoService;

    private final AsciiDocService asciiDocService;

    @Inject
    public DocumentationService(final RepoService repoService, final AsciiDocService asciiDocService) {
        this.repoService = Objects.requireNonNull(repoService);
        this.asciiDocService = Objects.requireNonNull(asciiDocService);
    }

    public Documentation getDocumentation(final String documentationId) throws IOException {
        final Path docPath = repoService.getLocalRepoPath().resolve(documentationId);
        final Set<Document> docs = Files.list(docPath)
                .filter(file -> file.toFile().getName().endsWith(ADOC_FILETYPE))
                .filter(file -> file.toFile().getName().startsWith(INDEX_FILE_PREFIX))
                .map(file -> createDocumentFromAdocFile(file))
                .collect(Collectors.toSet());

        final Set<Document> allDocs = new HashSet<>(docs);
        allDocs.add(createDefaultDocument(documentationId));
        
        final Set<Contributor> contributors = repoService.getContributors(documentationId);

        return new Documentation(documentationId, allDocs, contributors);
    }

    private Document createDocumentFromAdocFile(final Path file) {
        final String isoCode = file.toFile().getName().substring(6, file.toFile().getName().length() - 5);
        return createDocumentFromAdocFile(file, isoCode);
    }

    private Document createDocumentFromAdocFile(final Path file, final String isoCode) {
        final String tile = asciiDocService.getTitle(file);
        final String htmlContent = asciiDocService.convertToHtmlContent(file);
        return new Document(tile, isoCode, htmlContent);
    }

    private Document createDefaultDocument(final String documentationId) {
        final Path docPath = repoService.getLocalRepoPath().resolve(documentationId);
        final Path defaultFile = docPath.resolve(DEFAULT_INDEX_ADOC_NAME);
        return createDocumentFromAdocFile(defaultFile, EN_ISO_CODE);
    }

}
