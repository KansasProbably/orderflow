package io.github.kansasprobably.orderflow.config.cache;

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@EnableCaching
@EnableConfigurationProperties(ProductCacheProperties.class)
public class RedisCacheConfig {

    public static final String PRODUCTS_CACHE = "products";

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            ProductCacheProperties properties,
            RedisConnectionFactory redisConnectionFactory
    ) {

        RedisCacheWriter redisCacheWriter = RedisCacheWriter.create(
                redisConnectionFactory,
                RedisCacheWriter.RedisCacheWriterConfigurer::immediateWrites
        );
        RedisCacheConfiguration productCacheConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(properties.ttl())
                        .disableCachingNullValues()
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(RedisSerializer.json())
                        );

        return builder -> builder
                .cacheWriter(redisCacheWriter)
                .withCacheConfiguration(
                PRODUCTS_CACHE,
                productCacheConfiguration
        );
    }
}
