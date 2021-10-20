package net.adoptium.documentationservices.model;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Objects;

class DocumentTest {

    @Test
    public void testNullName() {
        //given
        final String name = null;
        final String isoCode = "de";
        final Locale locale = Locale.ENGLISH;

        //than
        Assertions.assertThrows(NullPointerException.class, () -> new Document(name, isoCode));
        Assertions.assertThrows(NullPointerException.class, () -> new Document(name, locale));
    }

    @Test
    public void testBlankName() {
        //given
        final String name = " ";
        final String isoCode = "de";
        final Locale locale = Locale.ENGLISH;

        //than
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Document(name, isoCode));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Document(name, locale));
    }

    @Test
    public void testNullLocale() {
        //given
        final String name = "name";
        final Locale locale = null;

        //than
        Assertions.assertThrows(NullPointerException.class, () -> new Document(name, locale));
    }

    @Test
    public void testNullIsoCode() {
        //given
        final String name = "name";
        final String isoCode = null;

        //than
        Assertions.assertThrows(NullPointerException.class, () -> new Document(name, isoCode));
    }

    @Test
    public void testInvalidIsoCode() {
        //given
        final String name = "name";
        final String isoCode = "invalid";

        //than
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Document(name, isoCode));
    }

    @Test
    public void testValidIsoCode() {
        //given
        final String name = "name";
        final String isoCode = "de";

        //when
        final Document document = new Document(name, isoCode);

        //than
        Assertions.assertEquals(Locale.GERMAN.getLanguage(), document.getLocale().getLanguage());
    }

    @Test
    public void testEquals() {
        //given
        final String name = "name";
        final String isoCode = "de";

        //when
        final Document document1 = new Document(name, isoCode);
        final Document document2 = new Document(name, isoCode);

        //than
        Assertions.assertTrue(Objects.equals(document1, document2));
        Assertions.assertTrue(Objects.equals(document2, document1));
    }

    @Test
    public void testNotEqualsForDifferentName() {
        //given
        final String name1 = "name1";
        final String name2 = "name2";
        final String isoCode = "de";

        //when
        final Document document1 = new Document(name1, isoCode);
        final Document document2 = new Document(name2, isoCode);

        //than
        Assertions.assertFalse(Objects.equals(document1, document2));
        Assertions.assertFalse(Objects.equals(document2, document1));
    }

    @Test
    public void testNotEqualsForDifferentIsoCode() {
        //given
        final String name = "name";
        final String isoCode1 = "de";
        final String isoCode2 = "en";

        //when
        final Document document1 = new Document(name, isoCode1);
        final Document document2 = new Document(name, isoCode2);

        //than
        Assertions.assertFalse(Objects.equals(document1, document2));
        Assertions.assertFalse(Objects.equals(document2, document1));
    }

    @Test
    public void testNotEqualsForDifferentLocale() {
        //given
        final String name = "name";
        final Locale locale1 = Locale.ENGLISH;
        final Locale locale2 = Locale.GERMAN;

        //when
        final Document document1 = new Document(name, locale1);
        final Document document2 = new Document(name, locale2);

        //than
        Assertions.assertFalse(Objects.equals(document1, document2));
        Assertions.assertFalse(Objects.equals(document2, document1));
    }

}
