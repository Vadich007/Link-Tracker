package backend.academy.bot.schemas.requests;

import jakarta.validation.constraints.NotNull;

public record KafkaEventRequest(@NotNull String action) {}
