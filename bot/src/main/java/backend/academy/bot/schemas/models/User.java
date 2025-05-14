package backend.academy.bot.schemas.models;

import backend.academy.bot.db.AddLinkRequestConverter;
import backend.academy.bot.schemas.requests.AddLinkRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private UserStates state;

    @Column(name = "add_link_request")
    @Convert(converter = AddLinkRequestConverter.class)
    private AddLinkRequest addLinkRequest;
}
