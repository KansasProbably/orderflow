package io.github.kansasprobably.orderflow.config.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.cache.products")
public record ProductCacheProperties(
        Duration ttl
) {
}
