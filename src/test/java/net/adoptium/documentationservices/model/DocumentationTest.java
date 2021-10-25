package net.adoptium.documentationservices.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

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
        final Collection<Document> documents = Set.of(new Document("title", "de"));

        //than
        Assertions.assertThrows(IllegalStateException.class, () -> new Documentation(id, documents));
    }

    @Test
    public void testValidCreationWithOnlyEn() {
        //given
        final String id = "ID";
        final Collection<Document> documents = Set.of(new Document("title", "en"));

        //than
        Assertions.assertDoesNotThrow(() -> new Documentation(id, documents));
    }

    @Test
    public void testValidCreation() {
        //given
        final String id = "ID";
        final Collection<Document> documents = Set.of(new Document("title", "de"), new Document("title", "en"));

        //than
        Assertions.assertDoesNotThrow(() -> new Documentation(id, documents));
    }

    @Test
    public void testNotEquals() {
        //given
        final String id1 = "ID1";
        final String id2 = "ID2";
        final Collection<Document> documents = Set.of(new Document("title", "en"));

        //when
        final Documentation documentation1 = new Documentation(id1, documents);
        final Documentation documentation2 = new Documentation(id2, documents);


        //than
        Assertions.assertFalse(Objects.equals(documentation1, documentation2));
        Assertions.assertFalse(Objects.equals(documentation2, documentation1));
    }

    @Test
    public void testEquals() {
        //given
        final String id = "ID";
        final Collection<Document> documents1 = Set.of(new Document("title", "en"));
        final Collection<Document> documents2 = Set.of(new Document("title", "en"), new Document("title", "de"));

        //when
        final Documentation documentation1 = new Documentation(id, documents1);
        final Documentation documentation2 = new Documentation(id, documents2);
        final Documentation documentation3 = new Documentation(id, documents2);


        //than
        Assertions.assertFalse(Objects.equals(documentation1, documentation2));
        Assertions.assertFalse(Objects.equals(documentation2, documentation1));
        Assertions.assertFalse(Objects.equals(documentation1, documentation3));
        Assertions.assertFalse(Objects.equals(documentation3, documentation1));
        Assertions.assertTrue(Objects.equals(documentation2, documentation3));
        Assertions.assertTrue(Objects.equals(documentation3, documentation2));
    }

}
