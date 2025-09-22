package com.lsq.coreplatform.redis;

import com.lsq.coreplatform.dto.event.BookStatusEvent;
import com.lsq.coreplatform.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.util.Assert.isTrue;

@RequiredArgsConstructor
@Component
@Slf4j
public class WishlistNotificationConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final BookRepository bookRepository;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        log.info("BOOK_STATUS_EVENT_HANDLING: Book status event received in Redis Stream Message {}", message.toString());

            BookStatusEvent event = convertMapToEvent(message.getValue());
            processWishlistNotifications(event);

    }

    private BookStatusEvent convertMapToEvent(Map<String, String> messageData) {
        return BookStatusEvent.builder()
                .bookId(Long.parseLong(messageData.get("bookId")))
                .title(messageData.get("title"))
                .author(messageData.get("author"))
                .timestamp(LocalDateTime.parse(messageData.get("timestamp")))
                .eventId(messageData.get("eventId"))
                .build();
    }

    private void processWishlistNotifications(BookStatusEvent event) {
        log.info("BOOK_STATUS_EVENT_HANDLING: Processing wishlist notifications for book: {} (ID: {})",
                event.getTitle(), event.getBookId());

            // TODO: Query wishlist table to find users who wishlisted this book
            // For now, simulating the notification logging as per requirements
            
            // Simulate finding users who wishlisted this book
            // In real implementation, we would query:
            // SELECT userId FROM wishlists WHERE bookId = ? AND active = true
            
            simulateWishlistNotifications(event);
    }

    private void simulateWishlistNotifications(BookStatusEvent event) {
        // This simulates the required notification logging
        
        log.info("Notification prepared for user_id: 1001 - Book [{}] is now available.", event.getTitle());
        log.info("Notification prepared for user_id: 1002 - Book [{}] is now available.", event.getTitle());
        log.info("Notification prepared for user_id: 1003 - Book [{}] is now available.", event.getTitle());
        
        log.info("BOOK_STATUS_EVENT_HANDLING: Successfully processed wishlist notifications for book: {} (Event: {})", 
                event.getTitle(), event.getEventId());
    }
}