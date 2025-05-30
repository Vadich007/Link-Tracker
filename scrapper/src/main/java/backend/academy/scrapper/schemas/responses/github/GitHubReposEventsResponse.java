package backend.academy.scrapper.schemas.responses.github;

import java.util.List;

public record GitHubReposEventsResponse(List<Event> events) {
}
