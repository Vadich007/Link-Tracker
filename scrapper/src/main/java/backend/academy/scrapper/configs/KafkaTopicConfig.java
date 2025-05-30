package backend.academy.scrapper.configs;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "kafka", ignoreUnknownFields = false)
public record KafkaTopicConfig(Topic topic) {
    public record Topic(
        @NotEmpty String chats, @NotEmpty String links, @NotEmpty String listLinks, @NotEmpty String events) {
    }
}
