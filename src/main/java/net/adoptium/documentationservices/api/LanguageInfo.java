package net.adoptium.documentationservices.api;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "POJO that represents a language.")
public class LanguageInfo {

    @Schema(required = true, example = "en", description = "iso-XXX based 2 char language code of this language")
    private String languageIsoCode;

    @Schema(required = true, example = "Deutsch", description = "The trasnslated name of the language")
    private String translatedName;

    @Schema(required = true, example = "de", description = "iso-XXX based 2 char language code of the language in that the name is translated")
    private String titleIsoCode;

}
