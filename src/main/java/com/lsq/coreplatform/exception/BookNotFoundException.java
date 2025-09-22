package com.lsq.coreplatform.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BookNotFoundException extends RuntimeException {
    private final Long bookId;
    public BookNotFoundException(Long bookId) {
        super("Book not found with ID: " + bookId);
        this.bookId = bookId;
    }
    public Long getBookId() { return bookId; }
}
