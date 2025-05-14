package backend.academy.scrapper.schemas.requests;

import backend.academy.scrapper.service.ValidateLink;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.hibernate.validator.constraints.URL;

public record AddLinkRequest(@URL @NotNull @ValidateLink String link, List<String> tags, List<String> filters) {}
