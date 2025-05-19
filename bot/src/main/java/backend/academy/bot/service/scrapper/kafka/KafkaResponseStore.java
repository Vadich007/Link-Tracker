package backend.academy.bot.service.scrapper.kafka;

import backend.academy.bot.schemas.responses.ListLinksResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.message-transport", havingValue = "kafka")
public class KafkaResponseStore {
    private final ConcurrentMap<Long, CompletableFuture<ListLinksResponse>> pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<ListLinksResponse> createRequest(Long chatId) {
        CompletableFuture<ListLinksResponse> future = new CompletableFuture<>();
        pendingRequests.put(chatId, future);
        return future;
    }

    public void completeRequest(Long chatId, ListLinksResponse response) {
        CompletableFuture<ListLinksResponse> future = pendingRequests.remove(chatId);
        if (future != null) {
            future.complete(response);
        }
    }
}
