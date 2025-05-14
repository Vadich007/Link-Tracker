package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.schemas.responses.github.Event;
import backend.academy.scrapper.schemas.responses.stackoverflow.Item;
import java.util.Set;

public interface LinkRepository {
    boolean containLink(String url);

    Set<Long> getChats(String url);

    void deleteLink(String url);

    void addLink(String url);

    Long getLinkId(String url);

    Set<String> getUrls(int limit, int offset);

    void deleteChat(String url, long chatId);

    void clear();

    int size();

    void setLastEvent(String url, Event event);

    void setLastEvent(String url, Item item);

    boolean isLastEvent(String url, Event event);

    boolean isLastEvent(String url, Long eventTime);

    Event getLastGitHubEvent(String url);

    Item getLastStackOverflowEvent(String url);
}
