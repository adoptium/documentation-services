package net.adoptium.documentationservices.api.schema;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "POJO that represents a contributor.")
public class ContributorInfo {

    @Schema(required = true, example = "john_doe", description = "Id of the github user")
    private String gitHubId;

    @Schema(required = true, example = "John Doe", description = "Name of the github user")
    private String name;

    @Schema(required = true, example = "https://avatars.githubusercontent.com/u/7557397", description = "Url of an avatar image of the github user")
    private String avatarUrl;

    public ContributorInfo() {
    }

    public ContributorInfo(final String gitHubId, final String name, final String avatarUrl) {
        this.gitHubId = gitHubId;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public String getGitHubId() {
        return gitHubId;
    }

    public void setGitHubId(final String gitHubId) {
        this.gitHubId = gitHubId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(final String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
