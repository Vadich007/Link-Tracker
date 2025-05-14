package backend.academy.scrapper.schemas.models;

import backend.academy.scrapper.schemas.orm.Links;
import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import org.hibernate.validator.constraints.URL;

public record Link(Long id, @URL @NotNull String url, List<String> tags, List<String> filters) {
    public static Link convertToLink(Links links) {
        String tags = links.subscriptions().get(0).tags();
        String filters = links.subscriptions().get(0).filters();
        return new Link(
                links.id(),
                links.url(),
                tags != null ? Arrays.stream(tags.split(",")).toList() : null,
                filters != null ? Arrays.stream(filters.split(",")).toList() : null);
    }
}
