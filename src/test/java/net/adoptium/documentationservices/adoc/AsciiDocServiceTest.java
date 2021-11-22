package net.adoptium.documentationservices.adoc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

class AsciiDocServiceTest {

    @Test
    public void testCreation() {
        Assertions.assertDoesNotThrow(() -> new AsciiDocService());
    }

    @Test
    public void testGetMetadataForNullPath() {
        //given
        final AsciiDocService service = new AsciiDocService();
        final Path pathToAdoc = null;

        //then
        Assertions.assertThrows(NullPointerException.class, () -> service.getMetadata(pathToAdoc));
    }

    @Test
    public void testGetMetadataForInvalidPath() throws URISyntaxException {
        //given
        final AsciiDocService service = new AsciiDocService();
        final Path pathToAdoc = getLocalPath("invalid.adoc");

        //then
        Assertions.assertThrows(AsciiDocException.class, () -> service.getMetadata(pathToAdoc));
    }

    @Test
    public void testConvertForInvalidPath() throws URISyntaxException {
        //given
        final AsciiDocService service = new AsciiDocService();
        final Path pathToAdoc = getLocalPath("invalid.adoc");

        //then
        Assertions.assertThrows(AsciiDocException.class, () -> service.convertToHtmlContent(pathToAdoc));
    }

    @Test
    public void testGetMetadata() throws URISyntaxException {
        //given
        final AsciiDocService service = new AsciiDocService();
        final Path pathToAdoc = getLocalPath("sample.adoc");

        //when
        final Map<String, String> metadata = service.getMetadata(pathToAdoc);

        //then
        Assertions.assertNotNull(metadata);
        Assertions.assertTrue(metadata.containsKey("description"));
        Assertions.assertTrue(metadata.containsKey("sectanchors"));
        Assertions.assertEquals("A test document.", metadata.get("description"));
        Assertions.assertEquals(null, metadata.get("sectanchors"));
    }

    @Test
    public void testConvert() throws URISyntaxException, IOException {
        //given
        final AsciiDocService service = new AsciiDocService();
        final Path pathToAdoc = getLocalPath("sample.adoc");
        final String expectedContent = Files.readString(getLocalPath("sample-converted.txt"));

        //when
        final String content = service.convertToHtmlContent(pathToAdoc);

        Assertions.assertNotNull(content);
        Assertions.assertEquals(expectedContent, content + System.lineSeparator()); //Auto Format adds newline to 'sample-converted.txt'
    }

    private Path getLocalPath(final String resourceName) {
        return Path.of(AsciiDocServiceTest.class.getResource("").getPath(), resourceName);
    }
}
