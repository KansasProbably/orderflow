package io.github.kansasprobably.orderflow.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableConfigurationProperties(KafkaTopicProperties.class)
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderConfirmedTopic(
            KafkaTopicProperties kafkaTopicProperties
    ) {
        return TopicBuilder
                .name(kafkaTopicProperties.orderConfirmed())
                .partitions(1)
                .replicas(1)
                .build();
    }
}
