package net.adoptium.documentationservices.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * A single document in a specific language
 */
public class Document {

    /**
     * The translated title of the document in the given locale
     */
    private final String title;

    /**
     * The locale that defines the language of the document (country is not used for now).
     */
    private final Locale locale;

    public Document(final String title, final String isoCode) {
        this.title = Objects.requireNonNull(title, "name must not be null");
        if(title.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank!");
        }
        Objects.requireNonNull(isoCode, "isoCode must not be null");
        final Locale localBasedOnIsoCode = new Locale(isoCode); // see Locale.getLanguage() javadoc to understand the creation of this instance
        this.locale = Arrays.asList(Locale.getAvailableLocales()).stream()
                .filter(l -> Objects.equals(localBasedOnIsoCode.getLanguage(), l.getLanguage()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Can not find locale for iso code '" + isoCode + "'"));
    }

    public Document(final String title, final Locale locale) {
        this.title = Objects.requireNonNull(title, "name must not be null");
        if(title.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank!");
        }
        this.locale = Objects.requireNonNull(locale, "locale must not be null");
    }

    public String getTitle() {
        return title;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Document document = (Document) o;
        return title.equals(document.title) && locale.equals(document.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, locale);
    }
}
