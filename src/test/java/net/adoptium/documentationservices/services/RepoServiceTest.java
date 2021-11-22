package net.adoptium.documentationservices.services;

import net.adoptium.documentationservices.model.Contributor;
import net.adoptium.documentationservices.model.Document;
import net.adoptium.documentationservices.model.Documentation;
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
import java.util.HashSet;
import java.util.Set;


public class RepoServiceTest {

    private static Path testDirectory;

    @Test
    public void testCreation() {
        Assertions.assertDoesNotThrow(() -> new RepoService("adoptium/documentation"));
    }

    @BeforeAll
    public static void setUpTempDirectory() {
        testDirectory = Paths.get("/tmp/" + RandomStringUtils.randomAlphanumeric(8));
        testDirectory.toFile().mkdirs();
        System.setProperty("jboss.server.data.dir", testDirectory.toString());
    }

    //@Test
    public void testUpdateCycle() throws IOException {

        final RepoService repoService = new RepoService("adoptium/documentation");
        boolean updateAvailable = repoService.isUpdateAvailable();
        Assertions.assertTrue(updateAvailable);

        Path downloadedData = repoService.downloadRepositoryContent();
        Assertions.assertTrue(Files.isDirectory(downloadedData));

        Assertions.assertTrue(Files.list(downloadedData).findAny().isPresent());

        repoService.saveLastUpdateTimestamp(Instant.now());

        updateAvailable = repoService.isUpdateAvailable();

        Assertions.assertFalse(updateAvailable);
    }


    @Test
    public void testGetContributors() throws IOException {
        final RepoService repoService = new RepoService("adoptium/documentation");
        final Document dummyDocument = new Document("index.adoc", "en");
        Set<Document> documents = new HashSet<>();
        documents.add(dummyDocument);
        final Documentation dummyDoc = new Documentation("installation", documents);
        Set<Contributor> contributors = repoService.getContributors(dummyDoc);

        Assertions.assertFalse(contributors.isEmpty());
    }


    @AfterAll
    public static void deleteTempDirectory() throws IOException {
        FileUtils.deleteQuietly(testDirectory.toFile());
    }

}
