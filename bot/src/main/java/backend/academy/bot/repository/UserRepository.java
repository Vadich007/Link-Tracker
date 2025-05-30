package backend.academy.bot.repository;

import backend.academy.bot.schemas.models.User;
import backend.academy.bot.schemas.models.UserStates;
import backend.academy.bot.schemas.requests.AddLinkRequest;
import java.util.List;

public interface UserRepository {
    boolean contain(long chatId);

    void addUser(long chatId);

    User getUser(long chatId);

    void updateState(long chatId, UserStates state);

    void addUrl(long chatId, String url);

    void addTags(long chatId, List<String> tags);

    void addFilters(long chatId, List<String> filters);

    AddLinkRequest getAddLinkRequest(long chatId);

    void deleteAddLinkRequest(long chatId);

    int size();
}
