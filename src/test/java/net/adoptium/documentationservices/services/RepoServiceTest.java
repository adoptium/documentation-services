package net.adoptium.documentationservices.services;

import net.adoptium.documentationservices.model.Contributor;
import net.adoptium.documentationservices.model.Document;
import net.adoptium.documentationservices.model.Documentation;
import net.adoptium.documentationservices.testutils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;


public class RepoServiceTest {

    @Test
    public void testCreation() {
        Assertions.assertDoesNotThrow(() -> new RepoService("adoptium/documentation"));
    }

    @Test
    public void testUpdateIsNotAvailableAfterInit() throws IOException {
        //given
        final RepoService repoService = new RepoService("adoptium/documentation");

        //then
        Assertions.assertFalse(repoService.isUpdateAvailable());
    }

    @Test
    public void testRepositoryDownload() throws IOException {
        //given
        final RepoService repoService = new RepoService("adoptium/documentation");
        final Path downloadedData = repoService.getLocalRepoPath();

        //when
        repoService.downloadRepositoryContent();

        //then
        Assertions.assertNotNull(downloadedData);
        Assertions.assertTrue(Files.isDirectory(downloadedData));
        Assertions.assertTrue(Files.list(downloadedData).count() > 0);

        //clear the temp folder
        repoService.clear();
    }

    @Test
    public void testRepositoryReDownload() throws IOException {
        //given
        final RepoService repoService = new RepoService("adoptium/documentation");
        final Path downloadedData = repoService.getLocalRepoPath();

        //when
        repoService.downloadRepositoryContent();

        //then
        Assertions.assertNotNull(downloadedData);
        Assertions.assertTrue(Files.isDirectory(downloadedData));
        Assertions.assertTrue(Files.list(downloadedData).count() > 0);

        //clear the temp folder
        repoService.clear();
    }

    @Test
    public void testDirectRepositoryUpdatesAfterDownload() throws IOException {
        //given
        final RepoService repoService = new RepoService("adoptium/documentation");

        //when
        repoService.downloadRepositoryContent();
        TestUtils.sleep(Duration.ofSeconds(12));

        //then
        Assertions.assertDoesNotThrow(() -> repoService.downloadRepositoryContent());

        //clear the temp folder
        repoService.clear();
    }

    /**
     * Note: this test can fail if changes are commited to the repo while the test is running.
     */
    @Test
    public void testRepositoryUpdatesAfterDownload() throws IOException {
        //given
        final RepoService repoService = new RepoService("adoptium/documentation");

        //when
        repoService.downloadRepositoryContent();
        TestUtils.sleep(Duration.ofSeconds(12));

        //then
        Assertions.assertFalse(repoService.isUpdateAvailable());

        //clear the temp folder
        repoService.clear();
    }

    @Test
    public void testClearWithoutDownload() {
        //given
        final RepoService repoService = new RepoService("adoptium/documentation");

        //then
        Assertions.assertDoesNotThrow(() -> repoService.clear());
    }

    @Test
    public void testGetContributors1() throws IOException {
        //given
        final RepoService repoService = new RepoService("adoptium/documentation");
        final Document dummyDocument = new Document("index.adoc", "en", "htmlContent");
        final Documentation dummyDoc = new Documentation("installation", Collections.singleton(dummyDocument));

        //when
        Set<Contributor> contributors = repoService.getContributors(dummyDoc);

        //then
        Assertions.assertFalse(contributors.isEmpty());
        Assertions.assertTrue(containsUser(contributors, "MBoegers"));
    }

    @Test
    public void testGetContributors2() throws IOException {
        //given
        final RepoService repoService = new RepoService("adoptium/documentation");
        final Document dummyDocument = new Document("index.adoc", "en", "htmlContent");
        final Documentation dummyDoc = new Documentation("test", Collections.singleton(dummyDocument));

        //when
        Set<Contributor> contributors = repoService.getContributors(dummyDoc);

        //then
        Assertions.assertFalse(contributors.isEmpty());
        Assertions.assertTrue(containsUser(contributors, "MBoegers"));
        Assertions.assertTrue(containsUser(contributors, "CKeibel"));
    }

    private boolean containsUser(Set<Contributor> contributors, String userName) {
        return contributors.stream()
                .map(contributor -> contributor.getGithubProfileURL())
                .filter(url -> url.startsWith("https://github.com/"))
                .filter(url -> url.length() > 19)
                .filter(url -> Objects.equals(userName, url.substring(19)))
                .count() > 0;
    }
}
