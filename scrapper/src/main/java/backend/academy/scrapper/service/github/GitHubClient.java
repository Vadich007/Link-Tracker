package backend.academy.scrapper.service.github;

import backend.academy.scrapper.configs.ApiConfig;
import backend.academy.scrapper.configs.ScrapperConfig;
import backend.academy.scrapper.schemas.responses.github.Event;
import backend.academy.scrapper.schemas.responses.github.GitHubReposEventsResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Arrays;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
@Slf4j
public class GitHubClient {
    private final RestTemplate restTemplate;
    private final ScrapperConfig config;
    private final ApiConfig apiConfig;

    @Retry(name = "scrapper")
    @CircuitBreaker(name = "scrapper")
    public GitHubReposEventsResponse sendEventResponse(String owner, String repos) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", config.githubToken());
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        String url = apiConfig.github().reposEvents().replace("{owner}", owner).replace("{repos}", repos);

        ResponseEntity<Event[]> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Event[].class);

        log.info("""
                Sent GET request {} {}
                Response received {}
                {}""",
            url, requestEntity.getHeaders(), response.getStatusCode(), response.getBody());

        return new GitHubReposEventsResponse(Arrays.asList(Objects.requireNonNull(response.getBody())));
    }
}
