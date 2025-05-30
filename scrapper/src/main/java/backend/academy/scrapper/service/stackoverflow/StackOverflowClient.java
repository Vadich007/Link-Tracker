package backend.academy.scrapper.service.stackoverflow;

import backend.academy.scrapper.configs.ApiConfig;
import backend.academy.scrapper.configs.ScrapperConfig;
import backend.academy.scrapper.schemas.responses.stackoverflow.StackOverflowResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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
public class StackOverflowClient {
    private final RestTemplate restTemplate;
    private final ScrapperConfig scrapperConfig;
    private final ApiConfig apiConfig;

    @Retry(name = "scrapper")
    @CircuitBreaker(name = "scrapper")
    public ResponseEntity<StackOverflowResponse> sendTimelineRequest(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(scrapperConfig.stackOverflow().accessToken());
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        String url = apiConfig
            .stackoverflow()
            .timeline()
            .replace("{question}", id)
            .replace("{key}", scrapperConfig.stackOverflow().key());

        ResponseEntity<StackOverflowResponse> response =
            restTemplate.exchange(url, HttpMethod.GET, requestEntity, StackOverflowResponse.class);

        log.info("""
            Sent GET request {}
            {}
            Response received {}
            {}""", url, requestEntity.getHeaders(), response.getStatusCode(), response.getBody());

        return response;
    }

    @Retry(name = "scrapper")
    @CircuitBreaker(name = "scrapper")
    public ResponseEntity<StackOverflowResponse> sendPostsRequest(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(scrapperConfig.stackOverflow().accessToken());
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        String url = apiConfig
            .stackoverflow()
            .posts()
            .replace("{post}", id)
            .replace("{key}", scrapperConfig.stackOverflow().key());

        ResponseEntity<StackOverflowResponse> response =
            restTemplate.exchange(url, HttpMethod.GET, requestEntity, StackOverflowResponse.class);

        log.info("""
            Sent GET request {}
            {}
            Response received {}
            {}""", url, requestEntity.getHeaders(), response.getStatusCode(), response.getBody());

        return response;
    }
}
