package net.adoptium.documentationservices.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A specific documentation (like "How to install Temurin").
 * A documentation that can have documents in multiple languages.
 */
public class Documentation {

    /**
     * Id based on the folder name of the documentation
     */
    private final String id;

    /**
     * Specific instances of the documentation in different languages (must contain at least 'en')
     */
    private final Set<Document> documents;

    private final Set<Contributor> contributors;

    public Documentation(final String id, final Collection<Document> documents, final Collection<Contributor> contributors) {
        this.id = Objects.requireNonNull(id);
        this.contributors = Collections.unmodifiableSet(new HashSet<>(contributors));
        if (this.id.isBlank()) {
            throw new IllegalArgumentException("ID of document must not be blank");
        }
        this.documents = Collections.unmodifiableSet(new HashSet<>(documents));

        //Check if at least english is present
        getEnglishDocument();
    }

    public String getId() {
        return id;
    }

    public Stream<Document> getDocuments() {
        return documents.stream();
    }

    public Stream<Contributor> getContributors() {
        return contributors.stream();
    }

    public Document getDocument(final Locale locale) {
        return Optional.ofNullable(locale)
                .map(l -> getDocuments().filter(d -> Objects.equals(l, d.getLocale())).findAny().orElse(null))
                .orElse(getEnglishDocument());
    }

    private Document getEnglishDocument() {
        return getDocuments().filter(d -> Objects.equals(Locale.ENGLISH.getLanguage(), d.getLocale().getLanguage()))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No english document provided for ID=" + id));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Documentation that = (Documentation) o;
        return id.equals(that.id) && documents.equals(that.documents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, documents);
    }
}
