package com.lsq.coreplatform.repository;

import com.lsq.coreplatform.dto.request.BookDTO;
import com.lsq.coreplatform.generated.tables.records.BooksRecord;

import java.time.Year;
import java.util.List;
import java.util.Set;

/**
 * Repository interface for book data access operations using jOOQ.
 * This repository provides data persistence and retrieval functionality for books
 * with emphasis on performance optimization and advanced search capabilities.
 * 
 * <p>Key Features:</p>
 * <ul>
 *   <li>Full-text search with MySQL MATCH AGAINST optimization</li>
 *   <li>Soft delete implementation for data integrity</li>
 *   <li>Batch operations for performance optimization</li>
 *   <li>Custom query optimization using jOOQ DSL</li>
 *   <li>Efficient existence checks with minimal data transfer</li>
 * </ul>
 * 
 * <p>Performance Considerations:</p>
 * <ul>
 *   <li>Leverages MySQL full-text indexes for search operations</li>
 *   <li>Uses batch operations to minimize database round trips</li>
 *   <li>Implements proper pagination to prevent memory issues</li>
 *   <li>Optimized queries with selective field retrieval</li>
 * </ul>
 * 
 * @author Library Management System
 * @version 1.0
 * @since 1.0
 */
public interface BookRepository {

    /**
     * Saves a book record to the database (create or update operation).
     * 
     * <p>Behavior:</p>
     * <ul>
     *   <li>If book.getId() is null: Performs INSERT operation</li>
     *   <li>If book.getId() exists: Performs UPDATE operation</li>
     *   <li>All fields except ID are updated in UPDATE operations</li>
     * </ul>
     * 
     * @param book the book record to save
     * @return true if the operation was successful, false otherwise
     * @throws org.jooq.exception.DataAccessException if database operation fails
     */
    Boolean save(BooksRecord book);

    /**
     * Performs soft delete on a single book by setting the isDeleted flag.
     * 
     * <p>Business Rules:</p>
     * <ul>
     *   <li>Only deletes books that are not currently borrowed</li>
     *   <li>Uses soft delete (sets isDeleted = 1) for data integrity</li>
     *   <li>Preserves historical data and referential integrity</li>
     * </ul>
     * 
     * @param bookId the unique identifier of the book to delete
     * @return true if the book was deleted, false if not found or borrowed
     * @throws org.jooq.exception.DataAccessException if database operation fails
     */
    Boolean delete(Long bookId);

    /**
     * Performs batch soft delete on multiple books for performance optimization.
     * 
     * <p>Performance Features:</p>
     * <ul>
     *   <li>Single UPDATE query with IN clause for efficiency</li>
     *   <li>Processes all eligible books in one database operation</li>
     *   <li>Returns count of actually deleted books</li>
     * </ul>
     * 
     * <p>Business Rules:</p>
     * <ul>
     *   <li>Applies same soft delete logic as single delete</li>
     *   <li>Skips borrowed books without failing the operation</li>
     *   <li>Handles non-existent IDs gracefully</li>
     * </ul>
     * 
     * @param bookIds set of book IDs to delete
     * @return number of books actually deleted (may be less than input size)
     * @throws org.jooq.exception.DataAccessException if database operation fails
     */
    int removeBooks(Set<Long> bookIds);

    /**
     * Searches for books using advanced full-text search with filtering and pagination.
     * 
     * <p>Search Algorithm:</p>
     * <ul>
     *   <li>Uses MySQL MATCH AGAINST for full-text search on title and author</li>
     *   <li>Supports natural language search mode for relevance ranking</li>
     *   <li>Combines text search with optional year filtering</li>
     *   <li>Automatically excludes soft-deleted books</li>
     * </ul>
     * 
     * <p>Performance Optimization:</p>
     * <ul>
     *   <li>Leverages MySQL full-text indexes (idx_books_title_author)</li>
     *   <li>Uses LIMIT and OFFSET for efficient pagination</li>
     *   <li>Returns DTOs directly without entity mapping overhead</li>
     * </ul>
     * 
     * @param searchText text to search in book title and author (empty string returns all)
     * @param publishedYear optional filter by publication year (null ignores filter)
     * @param limit maximum number of results to return (for pagination)
     * @param offset number of records to skip (for pagination)
     * @return set of BookDTO objects matching the search criteria
     * @throws org.jooq.exception.DataAccessException if database operation fails
     */
    Set<BookDTO> searchBooks(String searchText, Integer publishedYear, Integer limit, Integer offset);

    /**
     * Checks if a book with the given ISBN already exists in the system.
     * 
     * <p>Performance Features:</p>
     * <ul>
     *   <li>Uses EXISTS query for optimal performance</li>
     *   <li>Only checks ISBN field without retrieving full record</li>
     *   <li>Automatically excludes soft-deleted books</li>
     *   <li>Leverages unique index on ISBN for fast lookup</li>
     * </ul>
     * 
     * @param isbn the ISBN to check for existence
     * @return true if a book with this ISBN exists and is not deleted, false otherwise
     * @throws org.jooq.exception.DataAccessException if database operation fails
     */
    Boolean isbnExists(String isbn);

    /**
     * Fetches multiple book records by their IDs in a single query.
     * 
     * <p>Performance Features:</p>
     * <ul>
     *   <li>Single query with IN clause for batch retrieval</li>
     *   <li>Returns full BooksRecord objects for detailed processing</li>
     *   <li>Automatically excludes soft-deleted books</li>
     *   <li>Handles empty input gracefully</li>
     * </ul>
     * 
     * @param bookIds set of book IDs to retrieve
     * @return list of BooksRecord objects (may contain fewer items than requested)
     * @throws org.jooq.exception.DataAccessException if database operation fails
     */
    List<BooksRecord> fetchBooksByIds(Set<Long> bookIds);

    /**
     * Fetches a single book record by its unique identifier.
     * 
     * <p>Features:</p>
     * <ul>
     *   <li>Returns complete BooksRecord for full data access</li>
     *   <li>Automatically excludes soft-deleted books</li>
     *   <li>Uses primary key for optimal performance</li>
     * </ul>
     * 
     * @param bookId the unique identifier of the book to retrieve
     * @return the BooksRecord if found and not deleted, null otherwise
     * @throws org.jooq.exception.DataAccessException if database operation fails
     */
    BooksRecord fetchBookById(Long bookId);
}
