package backend.academy.scrapper.controller.http;

import backend.academy.scrapper.repository.chat.ChatRepository;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.constraints.Positive;
import java.util.NoSuchElementException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tg-chat/{id}")
@AllArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.message-transport", havingValue = "http")
public class ChatController {
    private final ChatRepository repository;

    @PostMapping
    @RateLimiter(name = "scrapper")
    public void addChat(@PathVariable @Positive long id) {
        if (repository.containChat(id)) throw new NoSuchElementException("Чат с таким id уже зарегистрирован");
        repository.addChat(id);
        log.info("Authorization of a user with an id {}", id);
    }
}
