package backend.academy.bot.configs;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "api", ignoreUnknownFields = false)
public record ApiConfig(ScrapperApi scrapper) {
    public record ScrapperApi(@NotEmpty String links, @NotEmpty String tgChat) {}
}
