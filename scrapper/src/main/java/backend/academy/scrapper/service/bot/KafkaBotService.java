package backend.academy.scrapper.service.bot;

import backend.academy.scrapper.configs.KafkaConfig;
import backend.academy.scrapper.schemas.requests.LinkUpdateRequest;
import backend.academy.scrapper.schemas.responses.bot.ListLinksResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.message-transport", havingValue = "kafka")
public class KafkaBotService implements BotService {
    private final KafkaTemplate<Long, Object> kafkaTemplate;
    private final KafkaConfig config;
    private long uniqueId = 0;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public void sendUpdate(String url, List<Long> ids, String message) {
        LinkUpdateRequest request = new LinkUpdateRequest(uniqueId, url, message, ids);
        uniqueId++;

        kafkaTemplate.send(config.topic().events(), uniqueId, objectMapper.writeValueAsString(request));

        log.info("Sent event with id {} in topic {}", uniqueId, config.topic().events());
    }

    @SneakyThrows
    public void getLinks(Long id, ListLinksResponse listLinksResponse) {
        kafkaTemplate.send(config.topic().listLinks(), id , objectMapper.writeValueAsString(listLinksResponse));
    }
}
