package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.monitor-service")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class MonitorServiceProperties {
    @Min(value = 1)
    private int batchSize;

    @Min(value = 1)
    private int launchInterval;

    @Min(value = 1)
    private int threads;
}
