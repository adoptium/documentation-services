package net.adoptium.documentationservices.services;

import net.adoptium.documentationservices.model.Contributor;
import net.adoptium.documentationservices.model.Documentation;
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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
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

    private static final String GITHUB_WEB_ADDRESS = "https://github.com/";
    private static final String TARGET_DIRECTORY = "adoptium_files/";
    private static final String METADATA_DIR = ".metadata";
    private static final String LAST_UPDATE_FILE = "last_update";
    private static final String ZIPBALL_SUFFIX = "/zipball";
    private static final String TIMEZONE_NAME_FOR_SAVED_TIMESTAMP = "UTC";
    private static final String ADOPTIUM_DOC_TEMP_DIR_PREFIX = "adoptium-doc";

    private final String repositoryName;

    private final GitHub github;

    private final Path localDataDir;

    private final Lock dataDirLock;

    @Inject
    public RepoService(@ConfigProperty(name = "documentation.repositoryName") final String repositoryName) {
        this.repositoryName = Objects.requireNonNull(repositoryName);
        final GitHubBuilder builder = new GitHubBuilder();

        try {
            github = builder.withOAuthToken("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").build();
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
        final Path lastUpdateFile = getTimestampFile();
        if (!Files.exists(lastUpdateFile)) {
            return true;
        }
        final Instant lastUpdateTimestamp = SyncUtils.executeSynchronized(dataDirLock, () -> loadDateFromFile(lastUpdateFile));

        //If last update is less than 1 min, we will never update
        if (lastUpdateTimestamp.plus(Duration.ofSeconds(10)).isAfter(Instant.now())) {
            return false;
        }
        final Instant repoLastUpdated = createGitHubRepository().getUpdatedAt().toInstant();
        return repoLastUpdated.isAfter(lastUpdateTimestamp);
    }

    /**
     * Downloads current main branch into local directory and returns directory reference.
     *
     * @throws IOException if there were problems downloading or saving the data.
     */
    public void downloadRepositoryContent() throws IOException {
        //Clear old content
        clear();

        final Instant timestamp = ZonedDateTime.now().toInstant();
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
            final Path timestampFile = getTimestampFile();
            if (!Files.exists(timestampFile.getParent())) {
                Files.createDirectories(timestampFile.getParent());
            }
            final String timestampStr = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of(TIMEZONE_NAME_FOR_SAVED_TIMESTAMP)).format(timestamp);
            Files.writeString(timestampFile, timestampStr, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
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
        });
    }

    /**
     * Returns all contributors that have worked on the given documentation
     *
     * @param documentation the documentations
     * @return all contributors
     * @throws IOException
     */
    public Set<Contributor> getContributors(final Documentation documentation) throws IOException {
        final GHRepository repo = createGitHubRepository();

        // iterate over all files for given documentation, that way we won't miss contributors of e.g. images.
        return repo.getDirectoryContent(documentation.getId()).stream()
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
        return localDataDir.resolve(TARGET_DIRECTORY);
    }

    /*
     * Reads timestamp from file and returns value as Instant.
     */
    private Instant loadDateFromFile(final Path file) throws IOException {
        final List<String> lines = Files.readAllLines(file);
        if (lines.isEmpty()) {
            throw new IllegalStateException("Timestamp file was empty.");
        }
        final String timestamp = lines.get(0);
        try {
            return Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of(TIMEZONE_NAME_FOR_SAVED_TIMESTAMP)).parse(timestamp));
        } catch (DateTimeParseException e) {
            throw new IllegalStateException("Timestamp file contained invalid data.", e);
        }
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
            return new Contributor(user.getName(), user.getAvatarUrl(), GITHUB_WEB_ADDRESS + user.getLogin());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read GitHub user", e);
        }
    }

    /*
     * Returns the file to be used to save the timestamp of the last update.
     */
    private Path getTimestampFile() {
        final Path metadataPath = localDataDir.resolve(METADATA_DIR);
        return metadataPath.resolve(LAST_UPDATE_FILE);
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
