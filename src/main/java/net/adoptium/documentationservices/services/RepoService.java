package net.adoptium.documentationservices.services;

import net.adoptium.documentationservices.model.Contributor;
import net.adoptium.documentationservices.util.SyncUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This service provides methods to retrieve data from GitHub using the GitHub API.
 */
@Singleton
public class RepoService {

    private static final Logger LOG = LoggerFactory.getLogger(RepoService.class);

    private static final String ADOPTIUM_DOC_TEMP_DIR_PREFIX = "adoptium-doc";

    private final String repositoryName;

    private final GitHub github;

    private final Path localDataDir;

    private final Lock dataDirLock;

    private ZonedDateTime lastUpdate;

    @Inject
    public RepoService(@ConfigProperty(name = "documentation.repositoryName") final String repositoryName) {
        this.repositoryName = Objects.requireNonNull(repositoryName);
        final GitHubBuilder builder = new GitHubBuilder();

        final String authToken = System.getenv("GITHUB_ACCESS_TOKEN");
        if (authToken != null && !authToken.isBlank()) {
            LOG.debug("Connecting to GitHub with access token");
            builder.withOAuthToken(authToken);
        }
        try {
            github = builder.build();
        } catch (final IOException e) {
            throw new RuntimeException("Can not instantiate GitHub API wrapper", e);
        }
        try {
            this.localDataDir = Files.createTempDirectory(ADOPTIUM_DOC_TEMP_DIR_PREFIX);
        } catch (final IOException e) {
            throw new RuntimeException("Can not create data dir", e);
        }
        dataDirLock = new ReentrantLock();
        try {
            downloadRepositoryContent();
        } catch (IOException e) {
            throw new IllegalStateException("Not able to download repo from Github", e);
        }
    }

    /**
     * Checks if the last update in the repository was after the saved timestamp.
     *
     * @return true if the last update is newer than the saved timestamp, false if not.
     * @throws IOException if problems occurred accessing the local filesystem or requesting information from GitHub.
     */
    public boolean isUpdateAvailable() throws IOException {
        final boolean updatedInLast10Seconds = Optional.ofNullable(lastUpdate)
                .map(timestamp -> timestamp.plus(Duration.ofSeconds(10)).isAfter(ZonedDateTime.now()))
                .orElse(false);

        if (updatedInLast10Seconds) {
            return false;
        }
        final Instant repoLastUpdated = createGitHubRepository().getUpdatedAt().toInstant();
        return repoLastUpdated.isAfter(lastUpdate.toInstant());
    }

    /**
     * Downloads current main branch into local directory and returns directory reference.
     *
     * @throws IOException if there were problems downloading or saving the data.
     */
    public void downloadRepositoryContent() throws IOException {
        //Clear old content
        clear();

        final ZonedDateTime timestamp = ZonedDateTime.now();
        final Path targetDirectory = getLocalRepoPath();

        SyncUtils.executeSynchronized(dataDirLock, () -> {

            //Download repo content & unzip as stream
            createGitHubRepository().readZip(input -> {
                try (ZipInputStream zipInputStream = new ZipInputStream(input)) {
                    ZipEntry zipEntry;
                    while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                        final Path filePath = targetDirectory.resolve(zipEntry.getName());
                        if (zipEntry.isDirectory()) {
                            filePath.toFile().mkdir();
                        } else {
                            filePath.toFile().getParentFile().mkdirs();
                            try (FileOutputStream fileOutputStream = new FileOutputStream(filePath.toFile())) {
                                int len;
                                byte[] content = new byte[1024];
                                while ((len = zipInputStream.read(content)) > 0) {
                                    fileOutputStream.write(content, 0, len);
                                }
                            }
                        }
                        zipInputStream.closeEntry();
                    }
                }
                return null;
            }, null);

            //The ZIP contains a folder that contains the project. Based on this we need to move everything 1 level up
            final Path zipRoot = Files.list(targetDirectory).findFirst().orElseThrow();
            Files.walkFileTree(zipRoot, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                    if (!Objects.equals(targetDirectory, dir) && !Objects.equals(targetDirectory, dir.getParent())) {
                        final Path newDir = targetDirectory.resolve(zipRoot.relativize(dir));
                        Files.createDirectory(newDir);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    final Path newFile = targetDirectory.resolve(zipRoot.relativize(file));
                    Files.move(file, newFile);
                    return FileVisitResult.CONTINUE;
                }
            });

            //save download time
            lastUpdate = timestamp;
        });
    }

    /**
     * Clears the local data dir (that holds the repo content locally)
     *
     * @throws IOException
     */
    public void clear() throws IOException {
        SyncUtils.executeSynchronized(dataDirLock, () -> {
            Files.walkFileTree(localDataDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                    if (!Objects.equals(dir, localDataDir)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            lastUpdate = null;
        });
    }

    public Optional<InputStream> readFile(final Path path) {
        Objects.requireNonNull(path, "path should not be null");
        final Path realPath = getLocalRepoPath().resolve(path);
        if (realPath.toFile().isFile()) {
            try {
                return Optional.ofNullable(Files.newInputStream(realPath));
            } catch (IOException e) {
                throw new IllegalStateException("Can nopt provide file " + path, e);
            }
        } else {
            return Optional.empty();
        }
    }

    public Set<Contributor> getContributors(final String documentationId) throws IOException {
        final GHRepository repo = createGitHubRepository();

        // iterate over all files for given documentation, that way we won't miss contributors of e.g. images.
        return repo.getDirectoryContent(documentationId).stream()
                .filter(ghContent -> ghContent.isFile())
                // retrieve commits for file and extract author
                .map(ghContent -> repo.queryCommits().path(ghContent.getPath()))
                .map(gHCommitQueryBuilder -> gHCommitQueryBuilder.list().spliterator())
                //Convert to Stream
                .flatMap(spliterator -> StreamSupport.stream(spliterator, false))
                //Get Author
                .map(commit -> getAuthor(commit))
                .filter(user -> user != null)
                //Convert to our data object
                .map(author -> toContributor(author))
                .collect(Collectors.toSet());
    }

    public Path getLocalRepoPath() {
        return localDataDir;
    }

    /**
     * Extracts the GitHub author
     *
     * @param commit the commit
     * @return the author
     */
    private GHUser getAuthor(final GHCommit commit) {
        try {
            return Objects.requireNonNull(commit, "commit must not be null").getAuthor();
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to retrieve author of commit " + commit.getSHA1(), e);

        }
    }

    /**
     * Converts GitHub usert to our data model
     *
     * @param user the GitHub user
     * @return the contributor
     */
    private Contributor toContributor(final GHUser user) {
        try {
            return new Contributor(user.getLogin(), user.getName(), user.getAvatarUrl());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read GitHub user", e);
        }
    }

    /**
     * Initializes GitHub connection and returns a repository instance.
     *
     * @return a repository instance
     * @throws IOException if GitHub REST API calls end in an error
     */
    private GHRepository createGitHubRepository() throws IOException {
        return github.getRepository(repositoryName);
    }
}
