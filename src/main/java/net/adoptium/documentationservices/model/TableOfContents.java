package net.adoptium.documentationservices.model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * The complete manual that contains multiple documentations.
 */
public class TableOfContents {

    /**
     * List of all documentations that are available
     */
    private final List<Documentation> documentations;

    public TableOfContents(final List<Documentation> documentations) {
        this.documentations = Collections.unmodifiableList(documentations);
    }

    public Stream<Documentation> getDocumentations() {
        return documentations.stream();
    }
}
