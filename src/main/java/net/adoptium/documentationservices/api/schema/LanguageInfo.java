package net.adoptium.documentationservices.api.schema;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "POJO that represents a language.")
public class LanguageInfo {

    @Schema(required = true, example = "en", description = "iso-XXX based 2 char language code of this language")
    private String languageIsoCode;

    @Schema(required = true, example = "Deutsch", description = "The trasnslated name of the language")
    private String translatedName;

    @Schema(required = true, example = "de", description = "iso-XXX based 2 char language code of the language in that the name is translated")
    private String titleIsoCode;

    public LanguageInfo() {
    }

    public LanguageInfo(final String languageIsoCode, final String translatedName, final String titleIsoCode) {
        this.languageIsoCode = languageIsoCode;
        this.translatedName = translatedName;
        this.titleIsoCode = titleIsoCode;
    }

    public String getLanguageIsoCode() {
        return languageIsoCode;
    }

    public void setLanguageIsoCode(final String languageIsoCode) {
        this.languageIsoCode = languageIsoCode;
    }

    public String getTranslatedName() {
        return translatedName;
    }

    public void setTranslatedName(final String translatedName) {
        this.translatedName = translatedName;
    }

    public String getTitleIsoCode() {
        return titleIsoCode;
    }

    public void setTitleIsoCode(final String titleIsoCode) {
        this.titleIsoCode = titleIsoCode;
    }
}
