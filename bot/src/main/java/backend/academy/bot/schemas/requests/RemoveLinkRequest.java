package backend.academy.bot.schemas.requests;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public record RemoveLinkRequest(@URL @NotNull String link) {
}
