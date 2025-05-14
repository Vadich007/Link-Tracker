package backend.academy.scrapper.schemas.requests;

import backend.academy.scrapper.service.ValidateLink;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public record RemoveLinkRequest(@URL @NotNull @ValidateLink String link) {}
