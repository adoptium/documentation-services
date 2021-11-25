package net.adoptium.documentationservices.model;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

class ContributorTest {

    @Test
    public void testNullName() throws MalformedURLException {
        //given
        final String name = null;
        final String githubAvatar = "some avatar";
        final URL githubProfile = new URL("http://www.github.com/hendrikebbers");

        //then
        Assertions.assertThrows(NullPointerException.class, () -> new Contributor(name, githubAvatar, githubProfile));
    }

    @Test
    public void testNullGitHubAvatar() throws MalformedURLException {
        //given
        final String name = "some name";
        final String githubAvatar = null;
        final URL githubProfile = new URL("http://www.github.com/hendrikebbers");

        //then
        Assertions.assertThrows(NullPointerException.class, () -> new Contributor(name, githubAvatar, githubProfile));
    }

    @Test
    public void testNullGitHubProfile() {
        //given
        final String name = "some name";
        final String githubAvatar = "some avatar";
        final URL githubProfile = null;

        //then
        Assertions.assertThrows(NullPointerException.class, () -> new Contributor(name, githubAvatar, githubProfile));
    }

    @Test
    public void testEquals() throws MalformedURLException {
        //given
        final String name = "some name";
        final String githubAvatar = "some avatar";
        final URL githubProfile = new URL("http://www.github.com/hendrikebbers");

        //when
        final Contributor contributor1 = new Contributor(name, githubAvatar, githubProfile);
        final Contributor contributor2 = new Contributor(name, githubAvatar, githubProfile);

        //then
        Assertions.assertEquals(contributor1, contributor2);
        Assertions.assertEquals(contributor2, contributor1);
    }

    @Test
    public void testNotEqualsForDifferentName() throws MalformedURLException {
        //given
        final String name1 = "some name";
        final String name2 = "some other name";
        final String githubAvatar = "some avatar";
        final URL githubProfile = new URL("http://www.github.com/hendrikebbers");

        //when
        final Contributor contributor1 = new Contributor(name1, githubAvatar, githubProfile);
        final Contributor contributor2 = new Contributor(name2, githubAvatar, githubProfile);

        //then
        Assertions.assertNotEquals(contributor1, contributor2);
        Assertions.assertNotEquals(contributor2, contributor1);
    }

    @Test
    public void testNotEqualsForDifferentAvatar() throws MalformedURLException {
        //given
        final String name = "some name";
        final String githubAvatar1 = "some avatar";
        final String githubAvatar2 = "some other avatar";
        final URL githubProfile = new URL("http://www.github.com/hendrikebbers");

        //when
        final Contributor contributor1 = new Contributor(name, githubAvatar1, githubProfile);
        final Contributor contributor2 = new Contributor(name, githubAvatar2, githubProfile);

        //then
        Assertions.assertNotEquals(contributor1, contributor2);
        Assertions.assertNotEquals(contributor2, contributor1);
    }

    @Test
    public void testNotEqualsForDifferentProfile() throws MalformedURLException {
        //given
        final String name = "some name";
        final String githubAvatar = "some avatar";
        final URL githubProfile1 = new URL("http://www.github.com/hendrikebbers");
        final URL githubProfile2 = new URL("http://www.github.com/abc");

        //when
        final Contributor contributor1 = new Contributor(name, githubAvatar, githubProfile1);
        final Contributor contributor2 = new Contributor(name, githubAvatar, githubProfile2);

        //then
        Assertions.assertNotEquals(contributor1, contributor2);
        Assertions.assertNotEquals(contributor2, contributor1);
    }

}
