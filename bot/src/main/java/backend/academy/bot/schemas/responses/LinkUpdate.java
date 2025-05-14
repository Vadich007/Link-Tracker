package backend.academy.bot.schemas.responses;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import org.hibernate.validator.constraints.URL;

public record LinkUpdate(@NotNull Long id, @NotNull @URL String url, String description, ArrayList<Long> tgChatIds) {}
