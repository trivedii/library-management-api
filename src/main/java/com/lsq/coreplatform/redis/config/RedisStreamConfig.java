package com.lsq.coreplatform.redis.config;

import com.lsq.coreplatform.redis.WishlistNotificationConsumer;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Configuration
public class RedisStreamConfig {

    @Value("${library.redis.book-status-topic:book-status-events}")
    private String topicName;

    private static final String CONSUMER_GROUP = "wishlist-notification-group";
    
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
    private Subscription subscription;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public Subscription bookStatusEventSubscription(RedisConnectionFactory redisConnectionFactory, 
                                                   WishlistNotificationConsumer consumer) throws UnknownHostException {
        
        // Simple consumer group creation - ignore if already exists
        try {
            redisConnectionFactory.getConnection().streamCommands()
                .xGroupCreate(topicName.getBytes(StandardCharsets.UTF_8), CONSUMER_GROUP, ReadOffset.from("0-0"), true);
            log.info("Created consumer group: {}", CONSUMER_GROUP);
        } catch (Exception e) {
            log.debug("Consumer group already exists or creation failed: {}", e.getMessage());
        }

        // Create container
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .<String, MapRecord<String, String, String>>builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        .build();

        this.container = StreamMessageListenerContainer.create(redisConnectionFactory, options);
        
        // Create subscription
        this.subscription = container.receiveAutoAck(
                Consumer.from(CONSUMER_GROUP, InetAddress.getLocalHost().getHostName()),
                StreamOffset.create(topicName, ReadOffset.lastConsumed()),
                consumer);

        container.start();
        return subscription;
    }

    @PreDestroy
    public void preDestroy() {
        log.info("Shutting down Redis Stream container");
        if (container != null) {
            container.stop();
        }
    }
}