package net.adoptium.documentationservices.adoc;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;

import javax.enterprise.context.ApplicationScoped;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class AsciiDocService {

    private final Asciidoctor asciidoctor;

    public AsciiDocService() {
        this.asciidoctor = Asciidoctor.Factory.create();
    }

    private Options createOptions(final String foldername) {
        return Options.builder()
                .headerFooter(false)
                .attributes(Attributes.builder().attribute("imagesdir", "http://localhost:9080/documentation/resources/" + foldername).build())
                .build();
    }

    private Document loadDocument(final Path pathToAdoc) {
        Objects.requireNonNull(pathToAdoc, "pathToAdoc should not be null!");
        try {
            return asciidoctor.loadFile(pathToAdoc.toFile(), createOptions(pathToAdoc.getParent().toFile().getName()));
        } catch (final Exception exception) {
            throw new AsciiDocException("Error in loading Asciidoc file '" + pathToAdoc + "'", exception);
        }
    }

    public String getTitle(final Path pathToAdoc) {
        return loadDocument(pathToAdoc).getTitle();
    }

    public Map<String, String> getMetadata(final Path pathToAdoc) {
        final Set<Map.Entry<String, Object>> entries = loadDocument(pathToAdoc).getAttributes()
                .entrySet();
        Map<String, String> metadata = new HashMap<>();
        entries.forEach(entry -> metadata.put(entry.getKey(), Optional.ofNullable(entry.getValue()).map(v -> v.toString()).filter(v -> !v.isBlank()).orElse(null)));
        return Collections.unmodifiableMap(metadata);
    }

    public String convertToHtmlContent(final Path pathToAdoc) {
        return loadDocument(pathToAdoc).convert();
    }

}
