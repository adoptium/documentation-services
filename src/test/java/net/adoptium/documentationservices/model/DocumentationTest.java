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
        final Collection<Contributor> contributors = Collections.emptyList();


        //then
        Assertions.assertThrows(NullPointerException.class, () -> new Documentation(id, documents, contributors));
    }

    @Test
    public void testBlankId() {
        //given
        final String id = "  ";
        final Collection<Document> documents = Collections.emptyList();
        final Collection<Contributor> contributors = Collections.emptyList();


        //then
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Documentation(id, documents, contributors));
    }

    @Test
    public void testNullCollection() {
        //given
        final String id = "ID";
        final Collection<Document> documents = null;
        final Collection<Contributor> contributors = Collections.emptyList();


        //then
        Assertions.assertThrows(NullPointerException.class, () -> new Documentation(id, documents, contributors));
    }

    @Test
    public void testEmptyCollection() {
        //given
        final String id = "ID";
        final Collection<Document> documents = Collections.emptyList();
        final Collection<Contributor> contributors = Collections.emptyList();


        //then
        Assertions.assertThrows(IllegalStateException.class, () -> new Documentation(id, documents, contributors));
    }

    @Test
    public void testCollectionWithoutEn() {
        //given
        final String id = "ID";
        final Collection<Document> documents = Set.of(new Document("title", "de", "htmlContent"));
        final Collection<Contributor> contributors = Collections.emptyList();


        //then
        Assertions.assertThrows(IllegalStateException.class, () -> new Documentation(id, documents, contributors));
    }

    @Test
    public void testValidCreationWithOnlyEn() {
        //given
        final String id = "ID";
        final Collection<Document> documents = Set.of(new Document("title", "en", "htmlContent"));
        final Collection<Contributor> contributors = Collections.emptyList();


        //then
        Assertions.assertDoesNotThrow(() -> new Documentation(id, documents, contributors));
    }

    @Test
    public void testValidCreation() {
        //given
        final String id = "ID";
        final Collection<Document> documents = Set.of(new Document("title", "de", "htmlContent"), new Document("title", "en", "htmlContent"));
        final Collection<Contributor> contributors = Collections.emptyList();

        //then
        Assertions.assertDoesNotThrow(() -> new Documentation(id, documents, contributors));
    }

    @Test
    public void testNotEquals() {
        //given
        final String id1 = "ID1";
        final String id2 = "ID2";
        final Collection<Document> documents = Set.of(new Document("title", "en", "htmlContent"));
        final Collection<Contributor> contributors = Collections.emptyList();

        //when
        final Documentation documentation1 = new Documentation(id1, documents, contributors);
        final Documentation documentation2 = new Documentation(id2, documents, contributors);


        //then
        Assertions.assertFalse(Objects.equals(documentation1, documentation2));
        Assertions.assertFalse(Objects.equals(documentation2, documentation1));
    }

    @Test
    public void testEquals() {
        //given
        final String id = "ID";
        final Collection<Document> documents1 = Set.of(new Document("title", "en", "htmlContent"));
        final Collection<Document> documents2 = Set.of(new Document("title", "en", "htmlContent"), new Document("title", "de", "htmlContent"));
        final Collection<Contributor> contributors = Collections.emptyList();

        //when
        final Documentation documentation1 = new Documentation(id, documents1, contributors);
        final Documentation documentation2 = new Documentation(id, documents2, contributors);
        final Documentation documentation3 = new Documentation(id, documents2, contributors);


        //then
        Assertions.assertFalse(Objects.equals(documentation1, documentation2));
        Assertions.assertFalse(Objects.equals(documentation2, documentation1));
        Assertions.assertFalse(Objects.equals(documentation1, documentation3));
        Assertions.assertFalse(Objects.equals(documentation3, documentation1));
        Assertions.assertTrue(Objects.equals(documentation2, documentation3));
        Assertions.assertTrue(Objects.equals(documentation3, documentation2));
    }

}
