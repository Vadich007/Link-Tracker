package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.schemas.models.Link;
import java.util.List;

public interface ChatRepository {
    boolean containChat(Long chatId);

    List<Link> getLinks(Long chatId);

    void deleteChat(Long chatId);

    void addChat(Long chatId);

    void subscribeLink(Long chatId, Link link);

    void clear();

    int size();
}
