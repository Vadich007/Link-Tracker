package backend.academy.bot.service;

import backend.academy.bot.configs.ApiConfig;
import backend.academy.bot.schemas.models.Link;
import backend.academy.bot.schemas.requests.AddLinkRequest;
import backend.academy.bot.schemas.requests.RemoveLinkRequest;
import backend.academy.bot.schemas.responses.LinkResponse;
import backend.academy.bot.schemas.responses.ListLinksResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapperClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ApiConfig apiConfig;

    public void registrationChat(long chatId) {
        restTemplate.postForEntity(
                apiConfig.scrapper().tgChat().replace("{id}", String.valueOf(chatId)), null, void.class);
        log.info("Sent POST request {}", apiConfig.scrapper().tgChat().replace("{id}", String.valueOf(chatId)));
    }

    public void deleteChat(long chatId) throws HttpClientErrorException {
        restTemplate.delete(apiConfig.scrapper().tgChat().replace("{id}", String.valueOf(chatId)));
        log.info("Sent DELETE request {}", apiConfig.scrapper().tgChat().replace("{id}", String.valueOf(chatId)));
    }

    public void trackLink(long chatId, AddLinkRequest request) throws HttpClientErrorException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        HttpEntity<AddLinkRequest> requestEntity = new HttpEntity<>(request, headers);
        ResponseEntity<LinkResponse> response =
                restTemplate.exchange(apiConfig.scrapper().links(), HttpMethod.POST, requestEntity, LinkResponse.class);

        log.info("Sent POST request {} \n {} \n {}", apiConfig.scrapper().links(), requestEntity.getHeaders(), request);
        log.info("Response received {} \n {}", response.getStatusCode(), response.getBody());
    }

    public void untrackLink(long chatId, RemoveLinkRequest request) throws HttpClientErrorException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        HttpEntity<RemoveLinkRequest> requestEntity = new HttpEntity<>(request, headers);
        ResponseEntity<LinkResponse> response = restTemplate.exchange(
                apiConfig.scrapper().links(), HttpMethod.DELETE, requestEntity, LinkResponse.class);

        log.info(
                "Sent DELETE request {} \n {} \n {}",
                apiConfig.scrapper().links(),
                requestEntity.getHeaders(),
                request);
        log.info("Response received {} \n {}", response.getStatusCode(), response.getBody());
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public List<Link> listLink(long chatId) throws HttpClientErrorException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<ListLinksResponse> response = restTemplate.exchange(
                apiConfig.scrapper().links(), HttpMethod.GET, requestEntity, ListLinksResponse.class);

        log.info("Sent GET request {} \n {}", apiConfig.scrapper().links(), requestEntity.getHeaders());
        log.info("Response received {} \n {}", response.getStatusCode(), response.getBody());

        return response.getBody() != null && response.getBody().links() != null
                ? response.getBody().links()
                : new ArrayList<>();
    }
}
