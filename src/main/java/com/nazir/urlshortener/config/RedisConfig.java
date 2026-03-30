package com.nazir.urlshortener.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis connection and template configuration.
 * <p>
 * Two templates are available:
 * <ul>
 *   <li>{@code RedisTemplate<String, Object>} — generic, for complex objects</li>
 *   <li>{@code StringRedisTemplate} — auto-configured by Spring Boot,
 *       used by {@link com.nazir.urlshortener.service.UrlCacheService}
 *       for manual JSON serialization</li>
 * </ul>
 * <p>
 * TTL configuration moved to {@link AppProperties.CacheProperties} in Phase 3.
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    // Phase 1 constants REMOVED — now configurable via:
    //   app.cache.url-ttl-hours (default: 24)
    //   app.cache.analytics-ttl-minutes (default: 5)
    // See: AppProperties.CacheProperties
}
