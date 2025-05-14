package backend.academy.bot.schemas.responses;

import backend.academy.bot.schemas.models.Link;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.hibernate.validator.constraints.URL;

public record LinkResponse(long id, @URL @NotNull String url, List<String> tags, List<String> filters) {
    public LinkResponse(Link link) {
        this(link.id(), link.url(), link.tags(), link.filters());
    }
}
