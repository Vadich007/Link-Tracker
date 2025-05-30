package backend.academy.scrapper.schemas.orm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subscriptions")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Subscriptions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "chat_id")
    private Long chatId;

    @ManyToOne
    @JoinColumn(name = "link_id", referencedColumnName = "id")
    private Links link;

    @Column(name = "tags")
    private String tags;

    @Column(name = "filters")
    private String filters;
}
