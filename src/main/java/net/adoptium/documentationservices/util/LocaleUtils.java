package net.adoptium.documentationservices.util;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class LocaleUtils {

    public static Locale getBasedOnIsoCode(final String isoCode) {
        Objects.requireNonNull(isoCode, "isoCode must not be null");
        final Locale localBasedOnIsoCode = new Locale(isoCode); // see Locale.getLanguage() javadoc to understand the creation of this instance
        return Arrays.asList(Locale.getAvailableLocales()).stream()
                .filter(l -> Objects.equals(localBasedOnIsoCode.getLanguage(), l.getLanguage()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Can not find locale for iso code '" + isoCode + "'"));
    }
}
