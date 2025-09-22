package com.lsq.coreplatform.dto.event;

import com.lsq.coreplatform.generated.enums.BooksAvailabilitystatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookStatusEvent {
    
    private Long bookId;
    private String title;
    private String author;
    private LocalDateTime timestamp;
    private String eventId;
    
    public static BookStatusEvent create(Long bookId, String title, String author) {
        return BookStatusEvent.builder()
                .bookId(bookId)
                .title(title)
                .author(author)
                .timestamp(LocalDateTime.now())
                .eventId(generateEventId(bookId))
                .build();
    }
    
    private static String generateEventId(Long bookId) {
        return "book-status-" + bookId + "-" + System.currentTimeMillis();
    }
}