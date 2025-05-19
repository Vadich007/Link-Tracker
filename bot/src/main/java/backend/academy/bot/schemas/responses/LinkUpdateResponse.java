package backend.academy.bot.schemas.responses;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import org.hibernate.validator.constraints.URL;

public record LinkUpdateResponse(
        @NotNull Long id, @NotNull @URL String url, String description, ArrayList<Long> tgChatIds) {}
