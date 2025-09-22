package com.lsq.coreplatform.repository;

import com.lsq.coreplatform.dto.request.BookDTO;
import com.lsq.coreplatform.generated.enums.BooksAvailabilitystatus;
import com.lsq.coreplatform.generated.tables.records.BooksRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.Year;
import java.util.*;

import static com.lsq.coreplatform.generated.Tables.BOOKS;

/**
 * Implementation of BookRepository using jOOQ for type-safe SQL generation.
 * This implementation provides optimized database operations with emphasis on
 * performance, data integrity, and advanced search capabilities.
 * 
 * <p>Implementation Features:</p>
 * <ul>
 *   <li>Type-safe SQL generation using jOOQ DSL</li>
 *   <li>Optimized queries with selective field projection</li>
 *   <li>MySQL-specific optimizations (full-text search, indexes)</li>
 *   <li>Batch operation support for performance</li>
 *   <li>Consistent soft delete implementation</li>
 * </ul>
 * 
 * <p>Database Schema Dependencies:</p>
 * <ul>
 *   <li>BOOKS table with full-text index on (title, author)</li>
 *   <li>Unique index on ISBN for fast existence checks</li>
 *   <li>Primary key on ID for efficient single record lookups</li>
 *   <li>isDeleted column for soft delete implementation</li>
 * </ul>
 * 
 * @author Library Management System
 * @version 1.0
 * @since 1.0
 * @see BookRepository
 * @see org.jooq.DSLContext
 */
@Repository
public class BookRepositoryImplementation implements  BookRepository {

    private final DSLContext dslContext;

    /**
     * Constructs a new BookRepositoryImplementation with the provided DSL context.
     * 
     * @param dslContext the jOOQ DSL context for database operations
     */
    public BookRepositoryImplementation(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementation Details:</p>
     * <ul>
     *   <li>INSERT: Uses jOOQ insertInto() with explicit field mapping</li>
     *   <li>UPDATE: Uses jOOQ update() with WHERE clause on primary key</li>
     *   <li>Returns execution result as boolean (affected rows > 0)</li>
     *   <li>Relies on database constraints for data validation</li>
     * </ul>
     * 
     * <p>Generated SQL Examples:</p>
     * <pre>
     * INSERT: INSERT INTO books (title, author, isbn, ...) VALUES (?, ?, ?, ...)
     * UPDATE: UPDATE books SET title=?, author=?, ... WHERE id=?
     * </pre>
     */
    @Override
    public Boolean save(BooksRecord book) {
        if (book.getId() == null) {
            return dslContext.insertInto(BOOKS)
                    .set(BOOKS.TITLE, book.getTitle())
                    .set(BOOKS.AUTHOR, book.getAuthor())
                    .set(BOOKS.ISBN, book.getIsbn())
                    .set(BOOKS.PUBLISHEDYEAR, book.getPublishedyear())
                    .set(BOOKS.AVAILABILITYSTATUS, book.getAvailabilitystatus())
                    .execute() > 0;
        } else {
            return dslContext.update(BOOKS)
                    .set(BOOKS.TITLE, book.getTitle())
                    .set(BOOKS.AUTHOR, book.getAuthor())
                    .set(BOOKS.ISBN, book.getIsbn())
                    .set(BOOKS.PUBLISHEDYEAR, book.getPublishedyear())
                    .set(BOOKS.AVAILABILITYSTATUS, book.getAvailabilitystatus())
                    .where(BOOKS.ID.eq(book.getId()))
                    .execute() > 0;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * <p>Implementation Details:</p>
     * <ul>
     *   <li>Uses UPDATE statement to set isDeleted = 1 (soft delete)</li>
     *   <li>Includes business logic in WHERE clause to prevent deleting borrowed books</li>
     *   <li>Returns true only if a record was actually updated</li>
     * </ul>
     * 
     * <p>Generated SQL:</p>
     * <pre>
     * UPDATE books SET isDeleted = 1 
     * WHERE id = ? AND availabilityStatus != 'Borrowed'
     * </pre>
     */
    @Override
    public Boolean delete(Long bookId) {
        return dslContext.update(BOOKS)
                .set(BOOKS.ISDELETED, (byte) 1)
                .where(BOOKS.ID.eq(bookId))
                .and(BOOKS.AVAILABILITYSTATUS.notEqual(BooksAvailabilitystatus.Borrowed))
                .execute() > 0;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementation Details:</p>
     * <ul>
     *   <li>Handles null/empty input gracefully by returning 0</li>
     *   <li>Uses single UPDATE with IN clause for optimal performance</li>
     *   <li>Does not enforce borrowed book restriction (handled at service layer)</li>
     *   <li>Returns actual count of updated records</li>
     * </ul>
     * 
     * <p>Generated SQL:</p>
     * <pre>
     * UPDATE books SET isDeleted = 1 WHERE id IN (?, ?, ?, ...)
     * </pre>
     */
    @Override
    public int removeBooks(Set<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return 0;
        }

        // Single update for all IDs
        return dslContext.update(BOOKS)
                .set(BOOKS.ISDELETED, (byte) 1)
                .where(BOOKS.ID.in(bookIds))
                .execute();
    }


    /**
     * {@inheritDoc}
     * 
     * <p>Implementation Details:</p>
     * <ul>
     *   <li>Uses MySQL MATCH AGAINST for full-text search with natural language mode</li>
     *   <li>Builds dynamic WHERE conditions based on provided parameters</li>
     *   <li>Combines text search and year filter with AND logic</li>
     *   <li>Always excludes soft-deleted books (isDeleted != 1)</li>
     *   <li>Uses selective field projection for optimal performance</li>
     *   <li>Directly maps results to BookDTO using jOOQ's fetchInto()</li>
     * </ul>
     * 
     * <p>Generated SQL Examples:</p>
     * <pre>
     * Text only: SELECT id, title, author, isbn, publishedYear FROM books 
     *           WHERE MATCH(title, author) AGAINST(? IN NATURAL LANGUAGE MODE) AND isDeleted != 1
     *           LIMIT ? OFFSET ?
     * 
     * Text + Year: ... WHERE MATCH(...) AND publishedYear = ? AND isDeleted != 1 ...
     * 
     * Year only: ... WHERE publishedYear = ? AND isDeleted != 1 ...
     * </pre>
     */
    @Override
    public Set<BookDTO> searchBooks(String searchText, Integer publishedYear, Integer limit, Integer offset) {

        Condition searchCondition = DSL.noCondition();

        // Add full-text search condition if searchText is provided
        if (searchText != null && !searchText.isEmpty()) {
            Field<Integer> match = DSL.field(
                    "MATCH({0}, {1}) AGAINST({2} IN NATURAL LANGUAGE MODE)",
                    Integer.class, BOOKS.TITLE, BOOKS.AUTHOR, DSL.val(searchText)
            );
            searchCondition = match.gt(0);
        }

        // Add published year condition if provided
        if (publishedYear != null) {
            Condition yearCondition = BOOKS.PUBLISHEDYEAR.eq(Year.of(publishedYear));
            searchCondition = searchCondition == DSL.noCondition()
                    ? yearCondition
                    : searchCondition.and(yearCondition); // AND if searchText is also present
        }

        // Build and execute query
        return new HashSet<>(dslContext.select(BOOKS.ID, BOOKS.TITLE, BOOKS.AUTHOR, BOOKS.ISBN, BOOKS.PUBLISHEDYEAR)
                .from(BOOKS)
                .where(searchCondition)
                .and(BOOKS.ISDELETED.notEqual((byte) 1))// only conditions that exist will be applied
                .limit(limit)
                .offset(offset)
                .fetchInto(BookDTO.class));
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementation Details:</p>
     * <ul>
     *   <li>Uses jOOQ's fetchExists() for optimal performance</li>
     *   <li>Generates EXISTS query instead of COUNT for efficiency</li>
     *   <li>Only checks non-deleted books (isDeleted = 0)</li>
     *   <li>Leverages unique index on ISBN for fast lookup</li>
     * </ul>
     * 
     * <p>Generated SQL:</p>
     * <pre>
     * SELECT EXISTS(SELECT 1 FROM books WHERE isbn = ? AND isDeleted = 0)
     * </pre>
     */
    @Override
    public Boolean isbnExists(String isbn) {
        return dslContext.fetchExists(
                DSL.selectOne()
                        .from(BOOKS)
                        .where(BOOKS.ISBN.eq(isbn))
                        .and(BOOKS.ISDELETED.eq((byte) 0))
        );
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementation Details:</p>
     * <ul>
     *   <li>Handles null/empty input by returning empty list</li>
     *   <li>Uses single query with IN clause for batch retrieval</li>
     *   <li>Returns full BooksRecord objects with all fields</li>
     *   <li>Filters out soft-deleted books automatically</li>
     * </ul>
     * 
     * <p>Generated SQL:</p>
     * <pre>
     * SELECT * FROM books WHERE id IN (?, ?, ...) AND isDeleted = 0
     * </pre>
     */
    @Override
    public List<BooksRecord> fetchBooksByIds(Set<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Collections.emptyList();
        }

        return dslContext.selectFrom(BOOKS)
                .where(BOOKS.ID.in(bookIds))
                .and(BOOKS.ISDELETED.eq((byte) 0))
                .fetch();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementation Details:</p>
     * <ul>
     *   <li>Uses primary key lookup for optimal performance</li>
     *   <li>Returns full BooksRecord with all field data</li>
     *   <li>Automatically excludes soft-deleted books</li>
     *   <li>Returns null if book not found or deleted</li>
     * </ul>
     * 
     * <p>Generated SQL:</p>
     * <pre>
     * SELECT * FROM books WHERE id = ? AND isDeleted = 0 LIMIT 1
     * </pre>
     */
    @Override
    public BooksRecord fetchBookById(Long bookId) {
        return dslContext.selectFrom(BOOKS)
                .where(BOOKS.ID.eq(bookId))
                .and(BOOKS.ISDELETED.eq((byte) 0))
                .fetchOne();
    }

}
