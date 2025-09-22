package com.lsq.coreplatform.service;

import com.lsq.coreplatform.dto.request.BookDTO;
import com.lsq.coreplatform.dto.response.BookSearchResponseDTO;
import com.lsq.coreplatform.dto.response.DeleteBooksResponseDTO;
import com.lsq.coreplatform.dto.response.ResponseDTO;
import com.lsq.coreplatform.exception.BookNotFoundException;
import com.lsq.coreplatform.exception.DuplicateBookException;
import com.lsq.coreplatform.exception.InvalidBookDataException;

import java.util.Set;

/**
 * Service interface for managing library book operations.
 * This service provides business logic for CRUD operations on books including
 * validation, data processing, and coordination with repository layer.
 * 
 * <p>Key Features:</p>
 * <ul>
 *   <li>Book creation with duplicate ISBN validation</li>
 *   <li>Book updates with status change notifications</li>
 *   <li>Advanced search with full-text search and filters</li>
 *   <li>Safe deletion with business rule enforcement</li>
 *   <li>Batch operations for bulk processing</li>
 * </ul>
 * 
 * @author Library Management System
 * @version 1.0
 * @since 1.0
 */
public interface BookSerivce {

    /**
     * Adds a new book to the library inventory.
     * 
     * <p>This method performs comprehensive validation including:</p>
     * <ul>
     *   <li>ISBN format validation (10 or 13 digits)</li>
     *   <li>ISBN uniqueness check</li>
     *   <li>Publication year validation (1450 - current year)</li>
     *   <li>Required field validation</li>
     * </ul>
     * 
     * @param bookDTO the book details to be saved
     * @return ResponseDTO containing success message and operation status
     * @throws DuplicateBookException if a book with the same ISBN already exists
     * @throws InvalidBookDataException if book data fails validation rules
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    ResponseDTO saveBook(BookDTO bookDTO);

    /**
     * Updates an existing book in the library inventory.
     * 
     * <p>This method:</p>
     * <ul>
     *   <li>Validates the book ID exists</li>
     *   <li>Performs the same validation as saveBook</li>
     *   <li>Checks if update is actually required</li>
     *   <li>Triggers asynchronous notifications for status changes to "Available"</li>
     * </ul>
     * 
     * <p><strong>Note:</strong> When a book status changes from "Borrowed" to "Available",
     * this triggers asynchronous wishlist notifications to users who have this book wishlisted.</p>
     * 
     * @param bookDTO the book details to be updated (must include valid book ID)
     * @return ResponseDTO containing success message and operation status
     * @throws BookNotFoundException if the book ID does not exist
     * @throws DuplicateBookException if ISBN change conflicts with existing book
     * @throws InvalidBookDataException if updated book data fails validation rules
     */
    ResponseDTO updateBook(BookDTO bookDTO);

    /**
     * Searches for books in the library inventory with advanced filtering and pagination.
     * 
     * <p>Search capabilities:</p>
     * <ul>
     *   <li>Full-text search across book title and author using MySQL MATCH AGAINST</li>
     *   <li>Publication year filtering</li>
     *   <li>Pagination support with configurable limit and offset</li>
     *   <li>Automatically excludes soft-deleted books</li>
     * </ul>
     * 
     * <p>Search Performance:</p>
     * <ul>
     *   <li>Uses MySQL full-text indexes for fast text search</li>
     *   <li>Optimized queries with proper indexing</li>
     *   <li>Supports natural language search mode</li>
     * </ul>
     * 
     * @param searchText text to search in book title and author (minimum 3 characters, maximum 255)
     * @param publishedYear optional filter by publication year
     * @param limit maximum number of results to return (1-100)
     * @param offset number of records to skip for pagination (0 or positive)
     * @return BookSearchResponseDTO containing matching books and pagination information
     * @throws IllegalArgumentException if search text is too short/long or pagination parameters are invalid
     */
    BookSearchResponseDTO searchBooks(String searchText, Integer publishedYear, Integer limit, Integer offset);

    /**
     * Deletes a single book from the library inventory using soft delete.
     * 
     * <p>Business Rules:</p>
     * <ul>
     *   <li>Books with status "Borrowed" cannot be deleted</li>
     *   <li>Uses soft delete (sets isDeleted flag) to maintain data integrity</li>
     *   <li>Validates book existence before deletion</li>
     * </ul>
     * 
     * @param bookId the unique identifier of the book to delete
     * @return ResponseDTO containing success message and operation status
     * @throws BookNotFoundException if the book ID does not exist
     * @throws IllegalStateException if the book is currently borrowed and cannot be deleted
     */
    ResponseDTO deleteBook(Long bookId);

    /**
     * Deletes multiple books from the library inventory in a single batch operation.
     * 
     * <p>Batch Processing Features:</p>
     * <ul>
     *   <li>Processes up to 100 books per request for performance</li>
     *   <li>Provides detailed results for each book ID</li>
     *   <li>Separates successfully deleted vs failed deletions</li>
     *   <li>Includes specific failure reasons for debugging</li>
     * </ul>
     * 
     * <p>Business Rules (same as single delete):</p>
     * <ul>
     *   <li>Books with status "Borrowed" are skipped with reason</li>
     *   <li>Non-existent book IDs are reported with reason</li>
     *   <li>Uses soft delete for data integrity</li>
     * </ul>
     * 
     * @param bookIds set of book IDs to delete (maximum 100 per request)
     * @return DeleteBooksResponseDTO containing detailed results:
     *         <ul>
     *           <li>deletedBookIds - successfully deleted book IDs</li>
     *           <li>notDeletedBookIds - failed deletion book IDs</li>
     *           <li>reasons - map of book ID to failure reason</li>
     *         </ul>
     * @throws IllegalArgumentException if more than 100 book IDs are provided
     */
    DeleteBooksResponseDTO deleteBook(Set<Long> bookIds);
}
