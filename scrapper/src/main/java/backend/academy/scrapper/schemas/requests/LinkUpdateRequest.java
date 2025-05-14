package backend.academy.scrapper.schemas.requests;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.hibernate.validator.constraints.URL;

public record LinkUpdateRequest(@NotNull Long id, @URL String url, String description, List<Long> tgChatIds) {}
