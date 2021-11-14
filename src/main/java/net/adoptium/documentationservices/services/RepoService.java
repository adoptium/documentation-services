package net.adoptium.documentationservices.services;

import net.adoptium.documentationservices.model.Contributor;
import net.adoptium.documentationservices.model.Documentation;
import org.kohsuke.github.*;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This service provides methods to retrieve data from GitHub using the GitHub API.
 *
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

    private GHRepository repository = null;

    // TODO - get the following from configuration
    private String repositoryName = "adoptium/documentation";
    private Proxy proxy = null;

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

        Instant lastUpdateTimestamp = loadDateFromFile(lastUpdateFile);
        Instant repoLastUpdated = getGitHubRepo().getUpdatedAt().toInstant();

        return repoLastUpdated.isAfter(lastUpdateTimestamp);
    }

    public Set<Contributor> getContributors(final Documentation documentation) throws IOException {
        final Set<GHUser> commitAuthors = new HashSet<>();
        final GHRepository repo = getGitHubRepo();

        // iterate over all files for given documentation, that way we won't miss contributors of e.g. images.
        final List<GHContent> documentationContents = getGitHubRepo().getDirectoryContent(documentation.getId());
        documentationContents.stream().filter(ghContent -> ghContent.isFile()).forEach(ghContent -> {
            // retrieve commits for file and extract author
            final GHCommitQueryBuilder commitQueryBuilder = repo.queryCommits();
            PagedIterable<GHCommit> commits = commitQueryBuilder.path(ghContent.getPath()).list();
            commits.forEach(ghCommit -> {
                try {
                    final GHUser author = ghCommit.getAuthor();
                    if (author != null) {
                        commitAuthors.add(author);
                    }
                } catch (IOException ioe) {
                    LOG.error("Failed to retrieve author of commit {}.", ghCommit.getSHA1());
                }
            });
        });

        // convert GHUsers to Contributors
        final Set<Contributor> contributors = new HashSet<>();
        commitAuthors.forEach(ghUser -> {
            try {
                final Contributor c = new Contributor(ghUser.getName(), ghUser.getAvatarUrl(), GITHUB_WEB_ADDRESS + ghUser.getLogin());
                contributors.add(c);
            } catch (IOException ioe) {
                LOG.error("Failed to retrieve details for GitHub user {}", ghUser.getLogin());
            }
        });
        return contributors;
    }


    /**
     * Saves the given timestamp to be used for the next update-available-check.
     *
     * @param timestamp the timestamp to save
     * @throws IOException if problems occurred accessing the local filesystem.
     */
    public void saveLastUpdateTimestamp(Instant timestamp) throws IOException {
        saveDateToFile(getTimestampFile(), timestamp);
    }


    /**
     * Downloads current main branch into local directory and returns directory reference.
     *
     * @return the path to the downloaded data
     * @throws IOException if there were problems downloading or saving the data.
     */
    public Path downloadRepositoryContent() throws IOException {
        final Path downloadedZipFile = downloadZipFile();

        // unzip to temporary directory
        final Path tempDirectory = getDataDir().resolve(TMP_DIRECTORY);
        new ZipFile(downloadedZipFile.toString()).extractAll(tempDirectory.toString());

        // delete existing copy
        final Path targetDirectory = getDataDir().resolve(TARGET_DIRECTORY);
        FileUtils.deleteDirectory(targetDirectory.toFile());

        // rename temporary to target
        Files.move(tempDirectory, targetDirectory, StandardCopyOption.ATOMIC_MOVE);

        // delete downloaded zip
        Files.delete(downloadedZipFile);

        return targetDirectory;
    }


    /*
     * Download ZIP of current repository main branch and returns filename of downloaded zip.
     */
    private Path downloadZipFile() throws IOException {
        final Path downloadedZipFile = getDataDir().resolve(TMP_FILE);

        final String archiveURL = getGitHubRepo().getUrl().toString() + ZIPBALL_SUFFIX;
        final URL url = new URL(archiveURL);
        final URLConnection connection;
        if (proxy != null) {
            connection = url.openConnection(proxy);
        } else {
            connection = url.openConnection();
        }
        try (InputStream urlInputStream = connection.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(downloadedZipFile.toString(), false)) {
            ReadableByteChannel urlInputChannel = Channels.newChannel(urlInputStream);
            fileOutputStream.getChannel().transferFrom(urlInputChannel, 0, Long.MAX_VALUE);
        }
        return downloadedZipFile;
    }

    /*
     * Saves given instant to file.
     */
    private void saveDateToFile(Path file, Instant timestamp) throws IOException {
        if (!Files.exists(file.getParent())) {
            Files.createDirectories(file.getParent());
        }
        final String timestampStr = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of(TIMEZONE_NAME_FOR_SAVED_TIMESTAMP)).format(timestamp);
        Files.writeString(file, timestampStr, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }


    /*
     * Reads timestamp from file and returns value as Instant.
     */
    private Instant loadDateFromFile(Path file) throws IOException {
        final List<String> lines = Files.readAllLines(file);
        if (lines.isEmpty()) {
            throw new IOException("Timestamp file was empty.");
        }
        String timestamp = lines.get(0);
        try {
            return Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of(TIMEZONE_NAME_FOR_SAVED_TIMESTAMP)).parse(timestamp));
        } catch (DateTimeParseException e) {
            throw new IOException("Timestamp file contained invalid data.");
        }
    }

    /*
     * Returns the directory used to store repository data locally.
     */
    private Path getDataDir() {
        return Path.of(System.getProperty("jboss.server.data.dir"));
    }


    /*
     * Returns the file to be used to save the timestamp of the last update.
     */
    private Path getTimestampFile() {
        final Path metadataPath = getDataDir().resolve(METADATA_DIR);
        return metadataPath.resolve(LAST_UPDATE_FILE);
    }


    /*
     * Retrieves the repository instance to be used everywhere in this class.
     */
    private GHRepository getGitHubRepo() throws IOException {
        if (repository == null) {
            initRepo();
        }
        return repository;
    }

    /*
     * Initializes GitHub connection and get repository instance.
     */
    private void initRepo() throws IOException {
        final GitHubBuilder builder = new GitHubBuilder();
        if (proxy != null) {
            builder.withProxy(proxy);
        }
        final GitHub github = builder.build();
        repository = github.getRepository(repositoryName);
    }
}
