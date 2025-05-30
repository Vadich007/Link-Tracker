package backend.academy.scrapper.service.bot;

import backend.academy.scrapper.configs.ApiConfig;
import backend.academy.scrapper.schemas.requests.LinkUpdateRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.message-transport", havingValue = "http")
public class HttpBotService implements BotService {
    private final RestTemplate restTemplate;
    private long uniqueId = 0;
    private final ApiConfig apiConfig;

    @Override
    @Retry(name = "scrapper")
    @CircuitBreaker(name = "scrapper")
    public void sendUpdate(String url, List<Long> ids, String message) {
        LinkUpdateRequest request = new LinkUpdateRequest(uniqueId, url, message, ids);
        uniqueId++;
        ResponseEntity<?> response = restTemplate.postForEntity(apiConfig.bot().update(), request, void.class);

        log.info("""
                Sent POST request {}
                {}
                Response received {}""",
            url, request, response.getStatusCode());
    }
}
