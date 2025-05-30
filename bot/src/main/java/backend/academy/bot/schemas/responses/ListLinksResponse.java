package backend.academy.bot.schemas.responses;

import backend.academy.bot.schemas.models.Link;
import java.util.List;

public record ListLinksResponse(List<Link> links, int size) {
}
