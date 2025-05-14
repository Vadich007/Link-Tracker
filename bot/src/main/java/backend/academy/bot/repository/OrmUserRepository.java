package backend.academy.bot.repository;

import backend.academy.bot.schemas.models.User;
import backend.academy.bot.schemas.models.UserStates;
import backend.academy.bot.schemas.requests.AddLinkRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "orm")
@RequiredArgsConstructor
public class OrmUserRepository implements UserRepository {
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public boolean contain(long chatId) {
        return entityManager.find(User.class, chatId) != null;
    }

    @Override
    @Transactional
    public void addUser(long chatId) {
        User user = new User();
        user.chatId(chatId);
        user.state(UserStates.FREE);
        entityManager.persist(user);
    }

    @Override
    public User getUser(long chatId) {
        return entityManager.find(User.class, chatId);
    }

    @Override
    @Transactional
    public void updateState(long chatId, UserStates state) {
        User user = entityManager.find(User.class, chatId);
        if (user != null) {
            user.state(state);
            entityManager.merge(user);
        }
    }

    @Override
    @Transactional
    public void addUrl(long chatId, String url) {
        User user = entityManager.find(User.class, chatId);
        if (user != null) {
            AddLinkRequest addLinkRequest = new AddLinkRequest();
            addLinkRequest.link(url);
            user.addLinkRequest(addLinkRequest);
            entityManager.merge(user);
        }
    }

    @Override
    @Transactional
    public void addTags(long chatId, List<String> tags) {
        User user = entityManager.find(User.class, chatId);
        if (user != null) {
            user.addLinkRequest().tags(tags);
            entityManager.merge(user);
        }
    }

    @Override
    @Transactional
    public void addFilters(long chatId, List<String> filters) {
        User user = entityManager.find(User.class, chatId);
        if (user != null) {
            user.addLinkRequest().filters(filters);
            entityManager.merge(user);
        }
    }

    @Override
    public AddLinkRequest getAddLinkRequest(long chatId) {
        return entityManager.find(User.class, chatId).addLinkRequest();
    }

    @Override
    @Transactional
    public void deleteAddLinkRequest(long chatId) {
        User user = entityManager.find(User.class, chatId);
        if (user != null) {
            user.addLinkRequest(null);
            entityManager.merge(user);
        }
    }

    @Override
    @Transactional
    public void clear() {
        entityManager.createQuery("DELETE FROM User").executeUpdate();
    }

    @Override
    public int size() {
        return entityManager
                .createQuery("SELECT COUNT(u) FROM User u", Long.class)
                .getSingleResult()
                .intValue();
    }
}
