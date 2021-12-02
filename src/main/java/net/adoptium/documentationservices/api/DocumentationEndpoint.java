package net.adoptium.documentationservices.api;

import net.adoptium.documentationservices.api.schema.ContributorInfo;
import net.adoptium.documentationservices.api.schema.DocumentInfo;
import net.adoptium.documentationservices.api.schema.LanguageInfo;
import net.adoptium.documentationservices.model.Document;
import net.adoptium.documentationservices.model.Documentation;
import net.adoptium.documentationservices.services.DocumentationService;
import net.adoptium.documentationservices.util.LocaleUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/api")
public class DocumentationEndpoint {

    @Inject
    private DocumentationService documentationService;

    @GET
    @Path("/{documentationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "returns document with all metadata", description = "this returns the document object for the given id in a language based on the request")
    public DocumentInfo getDocumentation(@Parameter(description = "Name of the document", required = true, example = "installation") @PathParam("documentationId") final String documentationId) {
        //TODO: Get language based on request (header) - see https://github.com/adoptium/documentation-services/issues/7
        final String languageIsoCode = "en";
        return getDocumentation(documentationId, languageIsoCode);
    }

    @GET
    @Path("/{documentationId}/{languageIsoCode}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "returns document with all metadata", description = "this returns the document object for the given id in a language based on the given iso code")
    public DocumentInfo getDocumentation(@Parameter(description = "Name of the document", required = true, example = "installation") @PathParam("documentationId") final String documentationId,
                                         @Parameter(description = "Iso code of language", required = true, example = "en") @PathParam("languageIsoCode") final String languageIsoCode) {
        try {
            final Locale locale = LocaleUtils.getBasedOnIsoCode(languageIsoCode);
            final Documentation documentation = documentationService.getDocumentation(documentationId);
            final Document document = documentation.getDocument(locale);
            final Set<LanguageInfo> supportedLanguages = documentation.getDocuments()
                    .map(doc -> new LanguageInfo(doc.getLocale().getISO3Language(), doc.getLocale().getDisplayLanguage(locale), languageIsoCode))
                    .collect(Collectors.toSet());
            final Set<ContributorInfo> contributors = documentation.getContributors()
                    .map(c -> new ContributorInfo(c.getName(), c.getName(), c.getGithubAvatar()))
                    .collect(Collectors.toSet());
            final DocumentInfo documentInfo = new DocumentInfo();
            documentInfo.setId(documentation.getId());
            documentInfo.setTitle(document.getTitle());
            documentInfo.setHtmlContent(document.getHtmlContent());
            documentInfo.setLanguageIsoCode(languageIsoCode);
            documentInfo.setSupportedLanguages(supportedLanguages);
            documentInfo.setContributors(contributors);
            return documentInfo;
        } catch (Exception e) {
            throw new RuntimeException("Can not get documentation", e);
        }
    }
}
