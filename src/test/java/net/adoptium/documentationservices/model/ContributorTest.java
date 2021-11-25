package net.adoptium.documentationservices.model;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

class ContributorTest {

    @Test
    public void testNullName() throws MalformedURLException {
        //given
        final String name = null;
        final String githubAvatar = "some avatar";
        final String githubId = "abc";

        //then
        Assertions.assertThrows(NullPointerException.class, () -> new Contributor(githubId, name, githubAvatar));
    }

    @Test
    public void testNullGitHubAvatar() throws MalformedURLException {
        //given
        final String name = "some name";
        final String githubAvatar = null;
        final String githubId = "abc";

        //then
        Assertions.assertThrows(NullPointerException.class, () -> new Contributor(githubId, name, githubAvatar));
    }

    @Test
    public void testNullGitHubProfile() {
        //given
        final String name = "some name";
        final String githubAvatar = "some avatar";
        final String githubId = null;

        //then
        Assertions.assertThrows(NullPointerException.class, () -> new Contributor(githubId, name, githubAvatar));
    }

    @Test
    public void testEquals() throws MalformedURLException {
        //given
        final String name = "some name";
        final String githubAvatar = "some avatar";
        final String githubId = "abc";

        //when
        final Contributor contributor1 = new Contributor(githubId, name, githubAvatar);
        final Contributor contributor2 = new Contributor(githubId, name, githubAvatar);

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
        final String githubId = "abc";

        //when
        final Contributor contributor1 = new Contributor(githubId, name1, githubAvatar);
        final Contributor contributor2 = new Contributor(githubId, name2, githubAvatar);

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
        final String githubId = "abc";

        //when
        final Contributor contributor1 = new Contributor(githubId, name, githubAvatar1);
        final Contributor contributor2 = new Contributor(githubId, name, githubAvatar2);

        //then
        Assertions.assertNotEquals(contributor1, contributor2);
        Assertions.assertNotEquals(contributor2, contributor1);
    }

    @Test
    public void testNotEqualsForDifferentProfile() throws MalformedURLException {
        //given
        final String name = "some name";
        final String githubAvatar = "some avatar";
        final String githubId1 = "abc";
        final String githubId2 = "123";

        //when
        final Contributor contributor1 = new Contributor(githubId1, name, githubAvatar);
        final Contributor contributor2 = new Contributor(githubId2, name, githubAvatar);

        //then
        Assertions.assertNotEquals(contributor1, contributor2);
        Assertions.assertNotEquals(contributor2, contributor1);
    }

}
