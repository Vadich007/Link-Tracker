package backend.academy.bot.service.scrapper;

import backend.academy.bot.configs.KafkaTopicConfig;
import backend.academy.bot.schemas.models.Link;
import backend.academy.bot.schemas.requests.AddLinkRequest;
import backend.academy.bot.schemas.requests.KafkaEventRequest;
import backend.academy.bot.schemas.requests.RemoveLinkRequest;
import backend.academy.bot.schemas.responses.ListLinksResponse;
import backend.academy.bot.service.scrapper.kafka.KafkaResponseStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

@RequiredArgsConstructor
@Service
@Slf4j
@ConditionalOnProperty(name = "app.message-transport", havingValue = "kafka")
public class ScrapperKafkaProducer implements ScrapperService {
    private final KafkaTemplate<Long, Object> kafkaTemplate;
    private final KafkaTopicConfig config;
    private final RedisTemplate<Long, List<Link>> redisTemplate;
    private final KafkaResponseStore kafkaResponseStore;

    @Override
    public void registrationChat(long chatId) {
        kafkaTemplate.send(config.topic().chats(), chatId, new KafkaEventRequest("REGISTRATION"));
        log.info(
            "Sent registration event for chatId {} in topic {}",
            chatId,
            config.topic().chats());
    }

    @SneakyThrows
    @Override
    public void trackLink(long chatId, AddLinkRequest request) {
        redisTemplate.delete(chatId);
        log.info("Disabling the cache for the user {}", chatId);

        kafkaTemplate.send(config.topic().links(), chatId, request);

        log.info(
            "Sent add link event for chatId {} in topic {}",
            chatId,
            config.topic().links());
    }

    @SneakyThrows
    @Override
    public void untrackLink(long chatId, RemoveLinkRequest request) {
        redisTemplate.delete(chatId);
        log.info("Disabling the cache for the user {}", chatId);

        kafkaTemplate.send(config.topic().links(), chatId, request);

        log.info(
            "Sent remove link event for chatId {} in topic {}",
            chatId,
            config.topic().links());
    }

    @SneakyThrows
    @Override
    public List<Link> listLink(long chatId) {

        List<Link> cachedData = redisTemplate.opsForValue().get(chatId);
        if (cachedData != null) {
            log.info("Data about the user's subscriptions and their cache has been received {}", chatId);
            return cachedData;
        }

        CompletableFuture<ListLinksResponse> future = kafkaResponseStore.createRequest(chatId);

        kafkaTemplate.send(config.topic().listLinks(), chatId, new KafkaEventRequest("GET_LINKS"));
        log.info(
            "Sent list links event for chatId {} in topic {}",
            chatId,
            config.topic().listLinks());

        ListLinksResponse response;
        try {
            response = future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new HttpServerErrorException(HttpStatusCode.valueOf(403));
        }

        List<Link> returnValue = response != null ? response.links() : new ArrayList<>();

        redisTemplate.opsForValue().set(chatId, returnValue);
        log.info("The list of the user's {} subscriptions is saved in the cache", chatId);

        return returnValue;
    }
}
