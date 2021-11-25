package net.adoptium.documentationservices.api.schema;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.HashSet;
import java.util.Set;

@Schema(description = "POJO that represents a translated document.")
public class DocumentInfo {

    @Schema(required = true, example = "32126319", description = "technical id of the document")
    private String id;

    @Schema(required = true, example = "en", description = "iso-XXX based 2 char language code of the document")
    private String languageIsoCode;

    @Schema(required = true, example = "How to install Temurin", description = "title of the document")
    private String title;

    @Schema(required = true, example = "<div class=\"paragraph\">content</div>", description = "in html rendered content of the document")
    private String htmlContent;

    @Schema(required = true, example = "TODO", description = "Last update of the document")
    private String lastUpdate;

    @Schema(required = true, description = "supported languages of the document")
    private Set<LanguageInfo> supportedLanguages = new HashSet<>();

    @Schema(required = true, description = "All contributors of the document")
    private Set<ContributorInfo> contributors = new HashSet<>();

    @Schema(required = true, description = "Url to edit the document")
    private String gitHubEditLink;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getLanguageIsoCode() {
        return languageIsoCode;
    }

    public void setLanguageIsoCode(final String languageIsoCode) {
        this.languageIsoCode = languageIsoCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(final String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(final String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Set<LanguageInfo> getSupportedLanguages() {
        return supportedLanguages;
    }

    public void setSupportedLanguages(final Set<LanguageInfo> supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }

    public String getGitHubEditLink() {
        return gitHubEditLink;
    }

    public void setGitHubEditLink(final String gitHubEditLink) {
        this.gitHubEditLink = gitHubEditLink;
    }

    public Set<ContributorInfo> getContributors() {
        return contributors;
    }

    public void setContributors(final Set<ContributorInfo> contributors) {
        this.contributors = contributors;
    }
}
