package net.adoptium.documentationservices.services;

import net.adoptium.documentationservices.adoc.AsciiDocService;
import net.adoptium.documentationservices.model.Documentation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class DocumentationServiceTest {

    @Test
    public void testGetDocumentation() throws IOException {
        //given
        final RepoService repoService = new RepoService("adoptium/documentation");
        final AsciiDocService asciiDocService = new AsciiDocService();
        final DocumentationService documentationService = new DocumentationService(repoService, asciiDocService);

        //when
        final Documentation documentation = documentationService.getDocumentation("documentation-vision");

        //
        Assertions.assertNotNull(documentation);
    }

}
