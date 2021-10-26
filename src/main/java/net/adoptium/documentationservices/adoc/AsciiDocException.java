package net.adoptium.documentationservices.adoc;

public class AsciiDocException extends RuntimeException {
    
    public AsciiDocException(final String message) {
        super(message);
    }

    public AsciiDocException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
