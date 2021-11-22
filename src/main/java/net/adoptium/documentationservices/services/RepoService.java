package net.adoptium.documentationservices.services;

import net.adoptium.documentationservices.model.Contributor;
import net.adoptium.documentationservices.model.Documentation;
import net.adoptium.documentationservices.util.SyncUtils;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
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

/**
 * This service provides methods to retrieve data from GitHub using the GitHub API.
 */
@ApplicationScoped
public class RepoService {

    private static final Logger LOG = LoggerFactory.getLogger(RepoService.class);

    private static final String GITHUB_WEB_ADDRESS = "https://github.com/";
    private static final String TARGET_DIRECTORY = "adoptium_files/";
    private static final String TMP_DIRECTORY = "adoptium_files_tmp/";
    private static final String TMP_FILE = "downloaded.zip";
    private static final String METADATA_DIR = ".metadata";
    private static final String LAST_UPDATE_FILE = "last_update";
    private static final String ZIPBALL_SUFFIX = "/zipball";
    private static final String TIMEZONE_NAME_FOR_SAVED_TIMESTAMP = "UTC";
    private static final String ADOPTIUM_DOC_TEMP_DIR_PREFIX = "adoptium-doc";

    private final String repositoryName;

    private final GitHub github;

    private final Proxy proxy = null;

    private final Path dataDir;

    private final Lock dataDirLock;

    @Inject
    public RepoService(@ConfigProperty(name = "documentation.repositoryName") final String repositoryName) {
        this.repositoryName = repositoryName;
        final GitHubBuilder builder = new GitHubBuilder();
        if (proxy != null) {
            builder.withProxy(proxy);
        }
        try {
            github = builder.build();
        } catch (final IOException e) {
            throw new RuntimeException("Can not instantiate GitHub API wrapper", e);
        }
        try {
            this.dataDir = Files.createTempDirectory(ADOPTIUM_DOC_TEMP_DIR_PREFIX);
        } catch (final IOException e) {
            throw new RuntimeException("Can not create data dir", e);
        }
        dataDirLock = new ReentrantLock();
    }

    /**
     * This constructor is needed to not end in the WELD-001410 issue:
     * "WELD-001410: The injection point has non-proxyable dependencies"
     * See http://stackoverflow.com/questions/12291945/ddg#34375558
     */
    public RepoService() {
        this(null);
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
        if (lastUpdateTimestamp.plus(Duration.ofMinutes(1)).isAfter(Instant.now())) {
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
    public Path downloadRepositoryContent() throws IOException {
        //Clear old content
        clear();

        final Instant timestamp = ZonedDateTime.now().toInstant();

        final Path downloadedZipFile = dataDir.resolve(TMP_FILE);
        final String archiveURL = createGitHubRepository().getUrl().toString() + ZIPBALL_SUFFIX;
        final URL url = new URL(archiveURL);
        final URLConnection connection;
        if (proxy != null) {
            connection = url.openConnection(proxy);
        } else {
            connection = url.openConnection();
        }
        final Path tempDirectory = dataDir.resolve(TMP_DIRECTORY);
        final Path targetDirectory = dataDir.resolve(TARGET_DIRECTORY);

        return SyncUtils.executeSynchronized(dataDirLock, () -> {
            //Download repo content
            try (final InputStream urlInputStream = connection.getInputStream();
                 final FileOutputStream fileOutputStream = new FileOutputStream(downloadedZipFile.toString(), false)) {
                final ReadableByteChannel urlInputChannel = Channels.newChannel(urlInputStream);
                fileOutputStream.getChannel().transferFrom(urlInputChannel, 0, Long.MAX_VALUE);
            }

            // unzip to temporary directory
            new ZipFile(downloadedZipFile.toString()).extractAll(tempDirectory.toString());

            // delete existing copy
            FileUtils.deleteDirectory(targetDirectory.toFile());

            // rename temporary to target
            Files.move(tempDirectory, targetDirectory, StandardCopyOption.ATOMIC_MOVE);

            // delete downloaded zip
            Files.delete(downloadedZipFile);

            //save download time
            final Path timestampFile = getTimestampFile();
            if (!Files.exists(timestampFile.getParent())) {
                Files.createDirectories(timestampFile.getParent());
            }
            final String timestampStr = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of(TIMEZONE_NAME_FOR_SAVED_TIMESTAMP)).format(timestamp);
            Files.writeString(timestampFile, timestampStr, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return targetDirectory;
        });
    }

    public void clear() throws IOException {
        SyncUtils.executeSynchronized(dataDirLock, () -> {
            Files.walkFileTree(dataDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                    if (!Objects.equals(dir, dataDir)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        });
    }

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
                .map(commit -> getUser(commit))
                .filter(user -> user != null)
                //Convert to our data object
                .map(author -> toContributor(author))
                .collect(Collectors.toSet());
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

    private GHUser getUser(final GHCommit commit) {
        try {
            return Objects.requireNonNull(commit, "commit must not be null").getAuthor();
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to retrieve author of commit " + commit.getSHA1(), e);
        }
    }

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
    private Path getTimestampFile() throws IOException {
        final Path metadataPath = dataDir.resolve(METADATA_DIR);
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
