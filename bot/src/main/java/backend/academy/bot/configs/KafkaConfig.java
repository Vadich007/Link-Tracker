package backend.academy.bot.configs;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "kafka", ignoreUnknownFields = false)
public record KafkaConfig(Topic topic) {
    public record Topic(@NotEmpty String chats, @NotEmpty String removeLink, @NotEmpty String addLink, @NotEmpty String listLinks, @NotEmpty String events) {}
}
