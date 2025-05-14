package backend.academy.scrapper.service.github;

import java.util.Arrays;

public enum GitHubEvents {
    PULL_REQUEST_EVENT("PullRequestEvent"),
    ISSUES_EVENT("IssuesEvent"),
    NONE("none");

    public final String event;

    GitHubEvents(String event) {
        this.event = event;
    }

    public static GitHubEvents getEnum(String event) {
        var enumEvent =
                Arrays.stream(values()).filter(e -> e.event.equals(event)).findFirst();
        return enumEvent.orElse(NONE);
    }
}
