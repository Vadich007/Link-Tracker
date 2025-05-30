package backend.academy.scrapper.schemas.responses.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PullRequest(String title, String body, User user) {
}
