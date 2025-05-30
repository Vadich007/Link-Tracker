package backend.academy.scrapper.configs;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "api", ignoreUnknownFields = false)
public record ApiConfig(GitHubApi github, StackOverflowApi stackoverflow, BotApi bot) {
    public record GitHubApi(@NotEmpty String reposEvents) {
    }

    public record StackOverflowApi(@NotEmpty String questions, @NotEmpty String timeline, @NotEmpty String posts) {
    }

    public record BotApi(@NotEmpty String update) {
    }
}
