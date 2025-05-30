package backend.academy.scrapper;

import backend.academy.scrapper.schemas.responses.github.Event;
import backend.academy.scrapper.schemas.responses.github.Issue;
import backend.academy.scrapper.schemas.responses.github.Payload;
import backend.academy.scrapper.schemas.responses.github.PullRequest;
import backend.academy.scrapper.schemas.responses.github.User;
import backend.academy.scrapper.service.github.GitHubService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GitHubServiceTests {
    private final GitHubService gitHubService = new GitHubService(null, null);

    @Test
    void getLastUpdateMessageOpenedLittleBodyPullRequestTest() {
        String url = "url";
        User user = new User("login");
        PullRequest pullRequest = new PullRequest("tittle", "body", user);
        Payload payload = new Payload("opened", pullRequest, null);
        Event event = new Event("id", "PullRequestEvent", payload, "createdAt");

        String actual = gitHubService.getLastUpdateMessage(url, event);
        String expect = String.format(
            """
                Новый Pull Request
                %s
                %s
                %s
                %s
                %s""",
            event.payload().pullRequest().title(),
            event.payload().pullRequest().user().login(),
            event.createdAt(),
            "body",
            url);

        Assertions.assertEquals(actual, expect);
    }

    @Test
    void getLastUpdateMessageNotOpenedLittleBodyPullRequestTest() {
        String url = "url";
        User user = new User("login");
        PullRequest pullRequest = new PullRequest("tittle", "body", user);
        Payload payload = new Payload("any", pullRequest, null);
        Event event = new Event("id", "PullRequestEvent", payload, "createdAt");

        String actual = gitHubService.getLastUpdateMessage(url, event);
        String expect = String.format("""
            Новое событие
            %s""", url);

        Assertions.assertEquals(actual, expect);
    }

    @Test
    void getLastUpdateMessageOpenedBigBodyPullRequestTest1() {
        String url = "url";
        User user = new User("login");
        String body = "1".repeat(201);
        PullRequest pullRequest = new PullRequest("tittle", body, user);
        Payload payload = new Payload("opened", pullRequest, null);
        Event event = new Event("id", "PullRequestEvent", payload, "createdAt");

        String actual = gitHubService.getLastUpdateMessage(url, event);
        String expect = String.format(
            """
                Новый Pull Request
                %s
                %s
                %s
                %s
                %s""",
            event.payload().pullRequest().title(),
            event.payload().pullRequest().user().login(),
            event.createdAt(),
            body.substring(0, 199),
            url);

        Assertions.assertEquals(actual, expect);
    }

    @Test
    void getLastUpdateMessageOpenedBigBodyPullRequestTest2() {
        String url = "url";
        User user = new User("login");
        String body = "1".repeat(200);
        PullRequest pullRequest = new PullRequest("tittle", body, user);
        Payload payload = new Payload("opened", pullRequest, null);
        Event event = new Event("id", "PullRequestEvent", payload, "createdAt");

        String actual = gitHubService.getLastUpdateMessage(url, event);
        String expect = String.format(
            """
                Новый Pull Request
                %s
                %s
                %s
                %s
                %s""",
            event.payload().pullRequest().title(),
            event.payload().pullRequest().user().login(),
            event.createdAt(),
            body.substring(0, 199),
            url);

        Assertions.assertEquals(actual, expect);
    }

    @Test
    void getLastUpdateMessageOpenedBigBodyPullRequestTest3() {
        String url = "url";
        User user = new User("login");
        String body = "1".repeat(199);
        PullRequest pullRequest = new PullRequest("tittle", body, user);
        Payload payload = new Payload("opened", pullRequest, null);
        Event event = new Event("id", "PullRequestEvent", payload, "createdAt");

        String actual = gitHubService.getLastUpdateMessage(url, event);
        String expect = String.format(
            """
                Новый Pull Request
                %s
                %s
                %s
                %s
                %s""",
            event.payload().pullRequest().title(),
            event.payload().pullRequest().user().login(),
            event.createdAt(),
            body,
            url);

        Assertions.assertEquals(actual, expect);
    }

    @Test
    void getLastUpdateMessageOpenedLittleBodyIssueTest() {
        String url = "url";
        User user = new User("login");
        Issue issue = new Issue("tittle", "body", user);
        Payload payload = new Payload("opened", null, issue);
        Event event = new Event("id", "IssuesEvent", payload, "createdAt");

        String actual = gitHubService.getLastUpdateMessage(url, event);
        String expect = String.format(
            """
                Новый Issue
                %s
                %s
                %s
                %s
                %s""",
            event.payload().issue().title(),
            event.payload().issue().user().login(),
            event.createdAt(),
            "body",
            url);

        Assertions.assertEquals(actual, expect);
    }

    @Test
    void getLastUpdateMessageNotOpenedIssueTest() {
        String url = "url";
        User user = new User("login");
        Issue issue = new Issue("tittle", "body", user);
        Payload payload = new Payload("any", null, issue);
        Event event = new Event("id", "IssuesEvent", payload, "createdAt");

        String actual = gitHubService.getLastUpdateMessage(url, event);
        String expect = String.format("""
            Новое событие
            %s""", url);

        Assertions.assertEquals(actual, expect);
    }

    @Test
    void getLastUpdateMessageOpenedNoneTest() {
        String url = "url";
        User user = new User("login");
        Issue issue = new Issue("tittle", "body", user);
        Payload payload = new Payload("opened", null, issue);
        Event event = new Event("id", "Issues111Event", payload, "createdAt");

        String actual = gitHubService.getLastUpdateMessage(url, event);
        String expect = String.format("""
            Новое событие
            %s""", url);

        Assertions.assertEquals(actual, expect);
    }

    @Test
    void getLastUpdateMessageOpenedBigBodyIssueTest1() {
        String url = "url";
        String body = "1".repeat(199);
        User user = new User("login");
        Issue issue = new Issue("tittle", body, user);
        Payload payload = new Payload("opened", null, issue);
        Event event = new Event("id", "IssuesEvent", payload, "createdAt");

        String actual = gitHubService.getLastUpdateMessage(url, event);
        String expect = String.format(
            """
                Новый Issue
                %s
                %s
                %s
                %s
                %s""",
            event.payload().issue().title(), event.payload().issue().user().login(), event.createdAt(), body, url);

        Assertions.assertEquals(actual, expect);
    }

    @Test
    void getLastUpdateMessageOpenedBigBodyIssueTest2() {
        String url = "url";
        String body = "1".repeat(200);
        User user = new User("login");
        Issue issue = new Issue("tittle", body, user);
        Payload payload = new Payload("opened", null, issue);
        Event event = new Event("id", "IssuesEvent", payload, "createdAt");

        String actual = gitHubService.getLastUpdateMessage(url, event);
        String expect = String.format(
            """
                Новый Issue
                %s
                %s
                %s
                %s
                %s""",
            event.payload().issue().title(),
            event.payload().issue().user().login(),
            event.createdAt(),
            body.substring(0, 199),
            url);

        Assertions.assertEquals(actual, expect);
    }

    @Test
    void getLastUpdateMessageOpenedBigBodyIssueTest3() {
        String url = "url";
        String body = "1".repeat(201);
        User user = new User("login");
        Issue issue = new Issue("tittle", body, user);
        Payload payload = new Payload("opened", null, issue);
        Event event = new Event("id", "IssuesEvent", payload, "createdAt");

        String actual = gitHubService.getLastUpdateMessage(url, event);
        String expect = String.format(
            """
                Новый Issue
                %s
                %s
                %s
                %s
                %s""",
            event.payload().issue().title(),
            event.payload().issue().user().login(),
            event.createdAt(),
            body.substring(0, 199),
            url);

        Assertions.assertEquals(actual, expect);
    }
}
