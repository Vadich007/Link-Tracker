package backend.academy.bot.schemas.models;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("Link")
public record Link(long id, @URL @NotNull String url, List<String> tags, List<String> filters) {
}
