package backend.academy.scrapper.configs;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "db", ignoreUnknownFields = false)
public record DbConfig(@NotEmpty String url, @NotEmpty String username, @NotEmpty String password) {
}
