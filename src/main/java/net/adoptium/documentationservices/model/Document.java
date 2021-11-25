package net.adoptium.documentationservices.model;

import net.adoptium.documentationservices.util.LocaleUtils;

import java.util.Locale;
import java.util.Objects;

/**
 * A single document in a specific language. This is normally based on exactly one adoc file.
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

    private final String htmlContent;

    public Document(final String title, final String isoCode, final String htmlContent) {
        this.title = Objects.requireNonNull(title, "name must not be null");
        this.htmlContent = Objects.requireNonNull(htmlContent, "htmlContent must not be null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank!");
        }
        this.locale = LocaleUtils.getBasedOnIsoCode(isoCode);
    }

    public Document(final String title, final Locale locale, final String htmlContent) {
        this.title = Objects.requireNonNull(title, "name must not be null");
        this.htmlContent = Objects.requireNonNull(title, "htmlContent must not be null");
        if (title.isBlank()) {
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

    public String getHtmlContent() {
        return htmlContent;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Document document = (Document) o;
        return title.equals(document.title) && locale.equals(document.locale) && htmlContent.equals(document.htmlContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, locale, htmlContent);
    }
}
