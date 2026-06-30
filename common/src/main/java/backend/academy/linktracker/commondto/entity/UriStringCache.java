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
@Entity
@NoArgsConstructor
@Table(name = "uri_string_cache")
public class UriStringCache {
    @Id
    @Column(name = "uri")
    private String uri;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    public UriStringCache(String uri, String value) {
        this.uri = uri;
        this.value = value;
    }
}
