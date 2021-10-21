package net.adoptium.documentationservices.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class DocumentationTest {

    @Test
    public void testNullId() {
        //given
        final String id = null;
        final Collection<Document> documents = Collections.emptyList();

        //than
        Assertions.assertThrows(NullPointerException.class, () -> new Documentation(id, documents));
    }

    @Test
    public void testBlankId() {
        //given
        final String id = "  ";
        final Collection<Document> documents = Collections.emptyList();

        //than
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Documentation(id, documents));
    }

    @Test
    public void testNullCollection() {
        //given
        final String id = "ID";
        final Collection<Document> documents = null;

        //than
        Assertions.assertThrows(NullPointerException.class, () -> new Documentation(id, documents));
    }

    @Test
    public void testEmptyCollection() {
        //given
        final String id = "ID";
        final Collection<Document> documents = Collections.emptyList();

        //than
        Assertions.assertThrows(IllegalStateException.class, () -> new Documentation(id, documents));
    }

    @Test
    public void testCollectionWithoutEn() {
        //given
        final String id = "ID";
        final Collection<Document> documents = Collections.singleton(new Document("title", "de"));

        //than
        Assertions.assertThrows(IllegalStateException.class, () -> new Documentation(id, documents));
    }

}
