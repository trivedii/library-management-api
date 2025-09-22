package com.lsq.coreplatform.exception;

import com.lsq.coreplatform.dto.response.ErrorResponseDTO;
import jakarta.validation.ConstraintViolationException;
import org.jooq.exception.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleBookNotFound(BookNotFoundException ex) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .errorCode("BOOK_NOT_FOUND")
                .message(ex.getMessage())
                .details(Map.of("bookId", ex.getBookId()))
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DuplicateBookException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateBook(DuplicateBookException ex) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .errorCode("DUPLICATE_BOOK")
                .message(ex.getMessage())
                .details(Map.of("isbn", ex.getIsbn()))
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidBookDataException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidBookData(InvalidBookDataException ex) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .errorCode("INVALID_BOOK_DATA")
                .message("Validation failed")
                .details(Map.of("field", ex.getField(), "error", ex.getValidationMessage()))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleDatabaseException(DataAccessException ex) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .errorCode("DB_ERROR")
                .message("Database operation failed: " + ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .errorCode("INVALID_ARGUMENT")
                .message("Invalid input: " + ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> violations = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage()
                ));

        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .errorCode("VALIDATION_FAILED")
                .message("Validation failed for request")
                .details(Map.of("violations", violations))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .errorCode("INTERNAL_ERROR")
                .message("An unexpected error occurred: " + ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


}

