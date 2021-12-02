package net.adoptium.documentationservices.model;

import java.util.Objects;

/**
 * A person who contributed to the documentation.
 */
public class Contributor {

    private final String name;

    private final String githubAvatar;

    private final String githubId;

    public Contributor(final String githubId, final String name, final String githubAvatar) {
        this.githubId = Objects.requireNonNull(githubId, "githubId must not be null");
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.githubAvatar = Objects.requireNonNull(githubAvatar, "avatar must not be null");
    }

    public String getName() {
        return name;
    }

    public String getGithubAvatar() {
        return githubAvatar;
    }

    public String getGithubId() {
        return githubId;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final Contributor otherContributor = (Contributor) other;
        return name.equals(otherContributor.name) && githubAvatar.equals(otherContributor.githubAvatar) && githubId.equals(otherContributor.githubId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, githubAvatar, githubId);
    }
}
