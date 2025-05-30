package backend.academy.scrapper.service.github;

import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.schemas.responses.github.Event;
import backend.academy.scrapper.schemas.responses.github.GitHubReposEventsResponse;
import backend.academy.scrapper.service.TrackedService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubService implements TrackedService<Event> {
    private final LinkRepository linkRepository;
    private final GitHubClient gitHubClient;

    @Override
    public String getServicePrefix() {
        return "https://github.com/";
    }

    @SuppressWarnings("StringSplitter")
    public boolean hasUpdate(String url) {
        String[] splittedUrl = url.split("/");
        String owner = splittedUrl[3], repos = splittedUrl[4];

        GitHubReposEventsResponse response = gitHubClient.sendEventResponse(owner, repos);

        if (response.events().isEmpty()) return false;

        Event event = response.events().getFirst();

        boolean result = !linkRepository.isLastEvent(url, event);

        if (result) linkRepository.setLastEvent(url, event);

        return result;
    }

    @SuppressFBWarnings(value = "VA_FORMAT_STRING_USES_NEWLINE")
    public String getLastUpdateMessage(String url, Event lastEvent) {
        String message = String.format("""
            Новое событие
            %s""", url);
        String preview;

        if (lastEvent.payload().action().equals("opened")) {
            switch (GitHubEvents.getEnum(lastEvent.type())) {
                case GitHubEvents.PULL_REQUEST_EVENT:
                    preview = lastEvent.payload().pullRequest().body().length() < 200
                        ? lastEvent.payload().pullRequest().body()
                        : lastEvent.payload().pullRequest().body().substring(0, 199);

                    message = String.format(
                        """
                            Новый Pull Request
                            %s
                            %s
                            %s
                            %s
                            %s""",
                        lastEvent.payload().pullRequest().title(),
                        lastEvent.payload().pullRequest().user().login(),
                        lastEvent.createdAt(),
                        preview,
                        url);
                    break;
                case GitHubEvents.ISSUES_EVENT:
                    preview = lastEvent.payload().issue().body().length() < 200
                        ? lastEvent.payload().issue().body()
                        : lastEvent.payload().issue().body().substring(0, 199);

                    message = String.format(
                        """
                            Новый Issue
                            %s
                            %s
                            %s
                            %s
                            %s""",
                        lastEvent.payload().issue().title(),
                        lastEvent.payload().issue().user().login(),
                        lastEvent.createdAt(),
                        preview,
                        url);
                    break;
                default:
                    break;
            }
        }
        return message;
    }
}
