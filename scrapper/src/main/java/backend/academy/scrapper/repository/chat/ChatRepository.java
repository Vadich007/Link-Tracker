package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.schemas.models.Link;

public interface ChatRepository {
    boolean containChat(Long chatId);

    void deleteChat(Long chatId);

    void addChat(Long chatId);

    void subscribeLink(Long chatId, Link link);

    int size();
}
