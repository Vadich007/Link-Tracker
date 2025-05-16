package backend.academy.bot.service.scrapper;

import backend.academy.bot.schemas.models.Link;
import backend.academy.bot.schemas.requests.AddLinkRequest;
import backend.academy.bot.schemas.requests.RemoveLinkRequest;
import java.util.List;

public interface ScrapperService {
    void registrationChat(long chatId);

    void trackLink(long chatId, AddLinkRequest request);

    void untrackLink(long chatId, RemoveLinkRequest request);

    List<Link> listLink(long chatId);
}
