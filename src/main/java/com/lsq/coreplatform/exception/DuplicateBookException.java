package com.lsq.coreplatform.exception;

public class DuplicateBookException extends RuntimeException {
    private final String isbn;
    public DuplicateBookException(String isbn) {
        super("Book with ISBN " + isbn + " already exists");
        this.isbn = isbn;
    }
    public String getIsbn() { return isbn; }
}

