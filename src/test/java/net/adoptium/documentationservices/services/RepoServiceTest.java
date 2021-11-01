package net.adoptium.documentationservices.services;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;


public class RepoServiceTest {

    private static Path testDirectory;

    @Test
    public void testCreation() {
        Assertions.assertDoesNotThrow(() -> new RepoService("test/test"));
    }

    @BeforeAll
    public static void setUpTempDirectory() {
        testDirectory = Paths.get("/tmp/" + RandomStringUtils.randomAlphanumeric(8));
        testDirectory.toFile().mkdirs();
        System.setProperty("jboss.server.data.dir", testDirectory.toString());
    }

    @Test
    public void testUpdateCycle() throws IOException {

        final RepoService repoService = new RepoService("test/test");
        boolean updateAvailable = repoService.isUpdateAvailable();
        Assertions.assertTrue(updateAvailable);

        Path downloadedData = repoService.downloadRepositoryContent();
        Assertions.assertTrue(Files.isDirectory(downloadedData));

        Assertions.assertTrue(Files.list(downloadedData).findAny().isPresent());

        repoService.saveLastUpdateTimestamp(Instant.now());

        updateAvailable = repoService.isUpdateAvailable();

        Assertions.assertFalse(updateAvailable);
    }

    @AfterAll
    public static void deleteTempDirectory() throws IOException {
        FileUtils.deleteQuietly(testDirectory.toFile());
    }

}
