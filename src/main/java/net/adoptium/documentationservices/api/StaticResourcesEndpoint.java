package net.adoptium.documentationservices.api;

import net.adoptium.documentationservices.services.RepoService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.file.Paths;

@Path("/resources")
public class StaticResourcesEndpoint {

    @Inject
    private RepoService repoService;

    @GET
    @Path("{path:.*}")
    public Response getResource(@PathParam("path") String path) {
        final MediaType mediaType;
        if (path.endsWith(".png")) {
            mediaType = new MediaType("image", "png");
        } else if (path.endsWith(".svg")) {
            mediaType = new MediaType("image", "svg+xml");
        } else {
            mediaType = MediaType.WILDCARD_TYPE;
        }
        return repoService.readFile(Paths.get(path))
                .map(inputStream -> Response.ok().entity(inputStream).type(mediaType).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }
}
