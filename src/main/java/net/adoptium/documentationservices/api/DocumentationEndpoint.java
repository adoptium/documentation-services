package net.adoptium.documentationservices.api;

import org.eclipse.microprofile.openapi.annotations.Operation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api")
public class DocumentationEndpoint {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "returns document with all metadata", description = "this returns the document object for the given id in a language based on the request")
    public DocumentInfo getDocumentation(final String documentationId) {
        //TODO: Get language based on request (header) - see https://github.com/adoptium/documentation-services/issues/7
        final String languageIsoCode = null;
        return getDocumentation(documentationId, languageIsoCode);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "returns document with all metadata", description = "this returns the document object for the given id in a language based on the given iso code")
    public DocumentInfo getDocumentation(final String documentationId, final String languageIsoCode) {
        //TODO: Call managed service(s) to get documentation metadata and create DocumentInfo object
        throw new RuntimeException("Not yet implemented");
    }
}
