package net.adoptium.documentationservices.api;

import org.microshed.testing.jaxrs.RESTClient;
import org.microshed.testing.testcontainers.ApplicationContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

// Tests will be added later. Some issues are currently discussed with testcontainers maintainer.
//@MicroShedTest
public class DocumentationEndpointTest {

    @Container
    public static ApplicationContainer app = new ApplicationContainer().withStartupTimeout(Duration.of(2, ChronoUnit.MINUTES)).withAppContextRoot("/myservice");

    @RESTClient
    public static DocumentationEndpoint documentationEndpoint;

    //@Test
    void getDocumentation() {
        documentationEndpoint.getDocumentation("");
    }
}
