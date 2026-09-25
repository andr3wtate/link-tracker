package backend.academy.linktracker.scrapper.messaging.kafka;

import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;

@Configuration
public class KafkaTopicConfiguration {
@ConditionalOnProperty(name = "app.messaging-type", havingValue = "kafka", matchIfMissing = true)
    @Bean
    public NewTopic linkUpdatesTopic(@Value("${app.kafka.topic}") String topic) {
        return TopicBuilder.name(topic)
                .partitions(3)
                .replicas(3)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                .build();
    }
}
