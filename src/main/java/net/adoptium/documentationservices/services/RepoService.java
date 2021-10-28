package net.adoptium.documentationservices.services;

import org.kohsuke.github.*;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;

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
import java.util.List;

/**
 * This service provides methods to retrieve data from GitHub using the GitHub API.
 *
 */
@ApplicationScoped
public class RepoService {

    private static final String TARGET_DIRECTORY = "adoptium_files/";
    private static final String TMP_DIRECTORY = "adoptium_files_tmp/";
    private static final String TMP_FILE = "downloaded.zip";
    private static final String METADATA_DIR = ".metadata";
    private static final String LAST_UPDATE_FILE = "last_update";

    private GHRepository repository = null;

    // TODO - get the following from configuration
    private String repositoryName = "adoptium/documentation";
    private Proxy proxy = null;

    /**
     * Checks if the last update in the repository was after the saved timestamp.
     *
     * @return true if the last update is newer than the saved timestamp, false if not.
     * @throws IOException
     */
    public boolean isUpdateAvailable() throws IOException {
        Path lastUpdateFile = getTimestampFile();
        if (!Files.exists(lastUpdateFile)) {
            return true;
        }

        Instant lastUpdateTimestamp = loadDateFromFile(lastUpdateFile);
        Instant repoLastUpdated = getGitHubRepo().getUpdatedAt().toInstant();

        return repoLastUpdated.isAfter(lastUpdateTimestamp);
    }


    /**
     * Saves the given timestamp to be used for the next update-available-check.
     *
     * @param timestamp
     * @throws IOException
     */
    public void saveLastUpdateTimestamp(Instant timestamp) throws IOException {
        saveDateToFile(getTimestampFile(), timestamp);
    }


    /**
     * Downloads current main branch into local directory and returns directory reference.
     *
     * @return
     * @throws IOException
     */
    public Path downloadRepositoryContent() throws IOException {
        Path downloadedZipFile = downloadZipFile();

        // unzip to temporary directory
        Path tempDirectory = getDataDir().resolve(TMP_DIRECTORY);
        new ZipFile(downloadedZipFile.toString()).extractAll(tempDirectory.toString());

        // delete existing copy
        Path targetDirectory = getDataDir().resolve(TARGET_DIRECTORY);
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
        Path downloadedZipFile = getDataDir().resolve(TMP_FILE);

        String archiveURL = getGitHubRepo().getUrl().toString() + "/zipball";
        URL url = new URL(archiveURL);
        URLConnection connection;
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
        String timestampStr = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC")).format(timestamp);
        Files.writeString(file, timestampStr, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }


    /*
     * Reads timestamp from file and returns value as Instant.
     */
    private Instant loadDateFromFile(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        if (lines.isEmpty()) {
            throw new IOException("Timestamp file was empty.");
        }
        String timestamp = lines.get(0);
        try {
            return Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC")).parse(timestamp));
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
        Path metadataPath = getDataDir().resolve(METADATA_DIR);
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
        GitHubBuilder builder = new GitHubBuilder();
        if (proxy != null) {
            builder.withProxy(proxy);
        }
        GitHub github = builder.build();
        repository = github.getRepository(repositoryName);
    }
}
