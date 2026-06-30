package backend.academy.linktracker.commondto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "string_long_cache")
public class StringLongCache {
    @Id
    @Column(name = "key")
    private String key;

    @Column(nullable = false)
    private Long value;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    public StringLongCache(String key, Long value) {
        this.key = key;
        this.value = value;
    }
}
