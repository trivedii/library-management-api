package com.lsq.coreplatform.redis;

import com.lsq.coreplatform.dto.event.BookStatusEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class BookEventPublisher {

    @Value("${library.redis.book-status-topic:book-status-events}")
    private String topicName;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void publishBookStatusEvent(BookStatusEvent event) {
        try {
            log.info("BOOK_STATUS_EVENT_HANDLING: Publishing book status event: BookId: {}",
                    event.getBookId());
            
            Map<String, Object> eventData = convertEventToMap(event);
            
            RecordId recordId = redisTemplate.opsForStream().add(topicName, eventData);
            
            log.info("BOOK_STATUS_EVENT_HANDLING: Published book status event with recordId: {}, BookId: {}", 
                    recordId, event.getBookId());
                    
        } catch (Exception e) {
            log.error("BOOK_STATUS_EVENT_HANDLING: Failed to publish book status event for bookId: {}", event.getBookId(), e);
            throw new RuntimeException("Failed to publish event to Redis Stream", e);
        }
    }

    private Map<String, Object> convertEventToMap(BookStatusEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("bookId", event.getBookId().toString());
        eventData.put("title", event.getTitle());
        eventData.put("author", event.getAuthor());
        eventData.put("timestamp", event.getTimestamp().toString());
        eventData.put("eventId", event.getEventId());
        return eventData;
    }
}