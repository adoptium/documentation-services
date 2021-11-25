package net.adoptium.documentationservices.testutils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.Objects;

public class TestUtils {

    /**
     * Sleeps for the given duration
     *
     * @param duration the duration
     */
    public static void sleep(Duration duration) {
        final long sleepInMillis = duration.toMillis();
        try {
            Thread.sleep(sleepInMillis);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupt while sleeping", e);
        }
    }

    /**
     * Deletes the given file. If it is a directory the dir and its content (recursivly) will be deleted.
     * This is only working if the file/dir is in the local tmp folder.
     *
     * @param path the path that should be deleted
     * @throws IOException
     */
    public static void deleteTempFile(final Path path) throws IOException {
        final String tempDir = System.getProperty("java.io.tmpdir");
        Objects.requireNonNull(tempDir, "error in finding temp dir");
        final Path tmpdirPath = Path.of(tempDir);
        if (!path.startsWith(tmpdirPath)) {
            throw new IllegalArgumentException("Path is not in temp dir: " + path);
        }

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
