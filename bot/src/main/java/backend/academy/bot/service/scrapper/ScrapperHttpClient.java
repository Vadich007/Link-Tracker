package backend.academy.bot.service.scrapper;

import backend.academy.bot.configs.ApiConfig;
import backend.academy.bot.schemas.models.Link;
import backend.academy.bot.schemas.requests.AddLinkRequest;
import backend.academy.bot.schemas.requests.RemoveLinkRequest;
import backend.academy.bot.schemas.responses.LinkResponse;
import backend.academy.bot.schemas.responses.ListLinksResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.message-transport", havingValue = "http")
public class ScrapperHttpClient implements ScrapperService {
    private final RestTemplate restTemplate;
    private final ApiConfig apiConfig;
    private static final String TG_CHAT_ID_HEADER = "Tg-Chat-Id";
    private final RedisTemplate<Long, List<Link>> redisTemplate;

    @Override
    @Retry(name = "bot")
    @CircuitBreaker(name = "bot")
    public void registrationChat(long chatId) {
        log.info("Sent POST request {}", apiConfig.scrapper().tgChat().replace("{id}", String.valueOf(chatId)));
        restTemplate.postForEntity(
            apiConfig.scrapper().tgChat().replace("{id}", String.valueOf(chatId)), null, void.class);
    }

    @Override
    @Retry(name = "bot")
    @CircuitBreaker(name = "bot")
    public void trackLink(long chatId, AddLinkRequest request) {
        redisTemplate.delete(chatId);
        log.info("Disabling the cache for the user {}", chatId);

        HttpHeaders headers = new HttpHeaders();
        headers.set(TG_CHAT_ID_HEADER, String.valueOf(chatId));
        HttpEntity<AddLinkRequest> requestEntity = new HttpEntity<>(request, headers);
        ResponseEntity<LinkResponse> response =
            restTemplate.exchange(apiConfig.scrapper().links(), HttpMethod.POST, requestEntity, LinkResponse.class);

        log.info("""
                Sent POST request {}
                {}
                {}
                Response received {}
                {}""",
            apiConfig.scrapper().links(), requestEntity.getHeaders(), request, response.getStatusCode(), response.getBody());
    }

    @Override
    @Retry(name = "bot")
    @CircuitBreaker(name = "bot")
    public void untrackLink(long chatId, RemoveLinkRequest request) {

        redisTemplate.delete(chatId);
        log.info("Disabling the cache for the user {}", chatId);

        HttpHeaders headers = new HttpHeaders();
        headers.set(TG_CHAT_ID_HEADER, String.valueOf(chatId));
        HttpEntity<RemoveLinkRequest> requestEntity = new HttpEntity<>(request, headers);
        ResponseEntity<LinkResponse> response = restTemplate.exchange(
            apiConfig.scrapper().links(), HttpMethod.DELETE, requestEntity, LinkResponse.class);

        log.info(
            """
                Sent DELETE request {}
                {}
                {}
                Response received {}
                {}""", apiConfig.scrapper().links(), requestEntity.getHeaders(), request, response.getStatusCode(), response.getBody());
    }

    @Override
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    @Retry(name = "bot")
    @CircuitBreaker(name = "bot")
    public List<Link> listLink(long chatId) {

        List<Link> cachedData = redisTemplate.opsForValue().get(chatId);
        if (cachedData != null) {
            log.info("Data about the user's subscriptions and their cache has been received {}", chatId);
            return cachedData;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(TG_CHAT_ID_HEADER, String.valueOf(chatId));
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<ListLinksResponse> response = restTemplate.exchange(
            apiConfig.scrapper().links(), HttpMethod.GET, requestEntity, ListLinksResponse.class);

        log.info("""
                Sent GET request {}
                {}
                Response received {}
                {}""",
            apiConfig.scrapper().links(), requestEntity.getHeaders(), response.getStatusCode(), response.getBody());

        List<Link> returnValue =
            response.getBody() != null && response.getBody().links() != null
                ? response.getBody().links()
                : new ArrayList<>();

        redisTemplate.opsForValue().set(chatId, returnValue);
        log.info("The list of the user's {} subscriptions is saved in the cache", chatId);

        return returnValue;
    }
}
