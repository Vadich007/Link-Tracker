package backend.academy.scrapper.schemas.responses.bot;

import backend.academy.scrapper.schemas.models.Link;
import java.util.List;

public record ListLinksResponse(List<Link> links, int size) {
}
