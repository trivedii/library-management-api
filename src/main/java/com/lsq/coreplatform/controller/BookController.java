package com.lsq.coreplatform.controller;

import com.lsq.coreplatform.dto.request.BookDTO;
import com.lsq.coreplatform.dto.response.BookSearchResponseDTO;
import com.lsq.coreplatform.dto.response.DeleteBooksResponseDTO;
import com.lsq.coreplatform.dto.response.ResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Interface for BookController defining RESTful endpoints for library book management.
 * This interface provides contract for CRUD operations on books including:
 * - Adding new books
 * - Updating existing books
 * - Searching books with filters and pagination
 * - Deleting books (single and batch operations)
 */
public interface BookController {

    /**
     * Adds a new book to the library inventory.
     *
     * @param bookDTO Book details to be added
     * @return ResponseEntity containing operation result
     */
    @PostMapping
    ResponseEntity<ResponseDTO> addBook(@Valid @RequestBody BookDTO bookDTO);

    /**
     * Updates an existing book in the library inventory.
     *
     * @param bookDTO Book details to be updated (must include book ID)
     * @return ResponseEntity containing operation result
     */
    @PatchMapping
    ResponseEntity<ResponseDTO> updateBook(@Valid @RequestBody BookDTO bookDTO);

    /**
     * Searches for books with optional filters and pagination.
     *
     * @param searchText Text to search in book title and author (minimum 3 characters)
     * @param publishedYear Filter books by publication year (optional)
     * @param limit Maximum number of results to return (1-100, default: 25)
     * @param offset Number of records to skip for pagination (default: 0)
     * @return ResponseEntity containing search results with pagination info
     */
    @GetMapping("/search")
    ResponseEntity<BookSearchResponseDTO> searchBooks(
            @RequestParam(defaultValue = "") String searchText,
            @RequestParam(required = false) Integer publishedYear,
            @RequestParam(defaultValue = "25") @Min(1) @Max(100) Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset
    );

    /**
     * Deletes a single book from the library inventory.
     *
     * @param bookId ID of the book to delete
     * @return ResponseEntity containing operation result
     */
    @DeleteMapping("/{bookId}")
    ResponseEntity<ResponseDTO> deleteBook(@PathVariable Long bookId);

    /**
     * Deletes multiple books from the library inventory in a single operation.
     *
     * @param bookIds Set of book IDs to delete (maximum 100 books per request)
     * @return ResponseEntity containing detailed results of batch deletion
     */
    @DeleteMapping("/delete-batch")
    ResponseEntity<DeleteBooksResponseDTO> deleteBooksInBatch(@RequestBody Set<Long> bookIds);
}