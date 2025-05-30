package backend.academy.scrapper.schemas.responses.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Payload(String action, @JsonProperty("pull_request") PullRequest pullRequest, Issue issue) {
}
