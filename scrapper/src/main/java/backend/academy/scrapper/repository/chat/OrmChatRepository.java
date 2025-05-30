package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.schemas.models.Link;
import backend.academy.scrapper.schemas.orm.Chats;
import jakarta.transaction.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "orm")
public interface OrmChatRepository extends ChatRepository, JpaRepository<Chats, Long> {

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO subscriptions (chat_id, link_id, tags, filters) " +
        "VALUES (:chatId, :linkId, :tags, :filters)", nativeQuery = true)
    void subscribe(
        @Param("chatId") Long chatId,
        @Param("linkId") Long linkId,
        @Param("tags") String tags,
        @Param("filters") String filters
    );

    @Override
    default boolean containChat(Long chatId) {
        return existsById(chatId);
    }

    @Override
    @Transactional
    default void deleteChat(Long chatId) {
        deleteById(chatId);
    }

    @Override
    @Transactional
    default void addChat(Long chatId) {
        save(new Chats().chatId(chatId));
    }

    @Override
    default void subscribeLink(Long chatId, Link link) {
        String tags = link.tags() != null ? String.join(",", link.tags()) : null;
        String filters = link.filters() != null ? String.join(",", link.filters()) : null;
        subscribe(chatId, link.id(), tags, filters);
    }

    @Override
    default int size() {
        return (int) count();
    }
}
