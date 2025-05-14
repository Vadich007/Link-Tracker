package backend.academy.scrapper.service.bot;

import backend.academy.scrapper.configs.ApiConfig;
import backend.academy.scrapper.schemas.requests.LinkUpdateRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class HttpBotService implements BotService {
    private final RestTemplate restTemplate = new RestTemplate();
    private long uniqueId = 0;
    private final ApiConfig apiConfig;

    @Override
    public void sendUpdate(String url, List<Long> ids, String message) {
        LinkUpdateRequest request = new LinkUpdateRequest(uniqueId, url, message, ids);
        uniqueId++;
        ResponseEntity<?> response = restTemplate.postForEntity(apiConfig.bot().update(), request, void.class);

        log.info("Sent POST request {} \n {}", url, request);
        log.info("Response received {}", response.getStatusCode());
    }
}
