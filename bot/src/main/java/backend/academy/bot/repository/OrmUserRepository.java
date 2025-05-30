package backend.academy.bot.repository;

import backend.academy.bot.schemas.models.User;
import backend.academy.bot.schemas.models.UserStates;
import backend.academy.bot.schemas.requests.AddLinkRequest;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "orm")
public interface OrmUserRepository extends UserRepository, JpaRepository<User, Long> {

    boolean existsById(Long id);

    @Override
    default boolean contain(long chatId) {
        return existsById(chatId);
    }

    @Override
    @Transactional
    default void addUser(long chatId) {
        User user = new User();
        user.chatId(chatId);
        user.state(UserStates.FREE);
        save(user);
    }

    @Override
    default User getUser(long chatId) {
        return findById(chatId).orElse(null);
    }

    @Override
    @Transactional
    default void updateState(long chatId, UserStates state) {
        findById(chatId).ifPresent(user -> {
            user.state(state);
            save(user);
        });
    }

    @Override
    @Transactional
    default void addUrl(long chatId, String url) {
        findById(chatId).ifPresent(user -> {
            AddLinkRequest addLinkRequest = new AddLinkRequest();
            addLinkRequest.link(url);
            user.addLinkRequest(addLinkRequest);
            save(user);
        });
    }

    @Override
    @Transactional
    default void addTags(long chatId, List<String> tags) {
        findById(chatId).ifPresent(user -> {
            user.addLinkRequest().tags(tags);
            save(user);
        });
    }

    @Override
    @Transactional
    default void addFilters(long chatId, List<String> filters) {
        findById(chatId).ifPresent(user -> {
            user.addLinkRequest().filters(filters);
            save(user);
        });
    }

    @Override
    default AddLinkRequest getAddLinkRequest(long chatId) {
        return findById(chatId)
            .map(User::addLinkRequest)
            .orElse(null);
    }

    @Override
    @Transactional
    default void deleteAddLinkRequest(long chatId) {
        findById(chatId).ifPresent(user -> {
            user.addLinkRequest(null);
            save(user);
        });
    }

    @Override
    default int size() {
        return (int) count();
    }
}
