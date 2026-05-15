package backend.academy.linktracker.commondto.entity;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "bot_users_sessions")
public class BotUserSession {
    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "chat_state", nullable = false)
    private String chatState;

    @Type(value = ListArrayType.class)
    @Column(name = "arguments", columnDefinition = "text[]", nullable = false)
    private List<String> arguments = new ArrayList<>();

    @Type(value = ListArrayType.class)
    @Column(name = "tags", columnDefinition = "text[]", nullable = false)
    private List<String> tags = new ArrayList<>();

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    public BotUserSession(Long chatId) {
        this.chatId = chatId;
    }
}
