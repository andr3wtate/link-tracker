package backend.academy.linktracker.commondto.entity;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "subscriptions",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "link_id"})})
public class Subscription {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private ScrapperUser scrapperUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id", nullable = false)
    private LinkEntity link;

    @Type(ListArrayType.class)
    @Column(name = "tags", columnDefinition = "text[]", nullable = false)
    private List<String> tags = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public Subscription(ScrapperUser scrapperUser, LinkEntity link, List<String> tags) {
        this.scrapperUser = scrapperUser;
        this.link = link;
        this.tags = tags;
    }
}
