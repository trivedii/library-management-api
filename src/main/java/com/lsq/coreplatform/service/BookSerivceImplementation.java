package com.lsq.coreplatform.service;

import com.lsq.coreplatform.dto.event.BookStatusEvent;
import com.lsq.coreplatform.dto.request.BookDTO;
import com.lsq.coreplatform.dto.response.BookSearchResponseDTO;
import com.lsq.coreplatform.dto.response.DeleteBooksResponseDTO;
import com.lsq.coreplatform.dto.response.ResponseDTO;
import com.lsq.coreplatform.exception.BookNotFoundException;
import com.lsq.coreplatform.exception.DuplicateBookException;
import com.lsq.coreplatform.exception.InvalidBookDataException;
import com.lsq.coreplatform.generated.enums.BooksAvailabilitystatus;
import com.lsq.coreplatform.generated.tables.records.BooksRecord;
import com.lsq.coreplatform.redis.BookEventPublisher;
import com.lsq.coreplatform.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.Year;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lsq.coreplatform.dto.response.Message.*;

/**
 * Implementation of BookService providing comprehensive book management functionality.
 * 
 * <p>This service implementation handles:</p>
 * <ul>
 *   <li>Book lifecycle management (create, read, update, delete)</li>
 *   <li>Business rule validation and enforcement</li>
 *   <li>Data transformation between DTOs and entities</li>
 *   <li>Integration with repository layer for data persistence</li>
 *   <li>Event publishing for asynchronous processing</li>
 * </ul>
 * 
 * <p>Key Design Features:</p>
 * <ul>
 *   <li>Comprehensive input validation with custom exceptions</li>
 *   <li>Optimistic updates with change detection</li>
 *   <li>Asynchronous event publishing for notifications</li>
 *   <li>Batch processing capabilities for performance</li>
 *   <li>Soft delete implementation for data integrity</li>
 * </ul>
 * 
 * @author Library Management System
 * @version 1.0
 * @since 1.0
 * @see BookSerivce
 * @see BookRepository
 * @see BookEventPublisher
 */
@Service
public class BookSerivceImplementation implements BookSerivce {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookEventPublisher bookEventPublisher;

    /**
     * {@inheritDoc}
     * 
     * <p>Implementation Details:</p>
     * <ul>
     *   <li>Validates book data using comprehensive business rules</li>
     *   <li>Checks for ISBN uniqueness in the database</li>
     *   <li>Creates new BooksRecord and persists to database</li>
     *   <li>Returns standardized success response</li>
     * </ul>
     */
    @Override
    public ResponseDTO saveBook(BookDTO bookDTO) {

        validateBookDetails(bookDTO);

        BooksRecord bookRecord = new BooksRecord();
        bookRepository.save(mapToBookRecord(bookDTO, bookRecord));

        return ResponseDTO.builder()
                .message(SAVE_SUCCESSFUL.getMessage())
                .build();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementation Details:</p>
     * <ul>
     *   <li>Retrieves existing book record from database</li>
     *   <li>Performs change detection to avoid unnecessary updates</li>
     *   <li>Captures old availability status for event publishing</li>
     *   <li>Publishes asynchronous events for status changes to "Available"</li>
     *   <li>Uses optimistic locking approach for concurrent updates</li>
     * </ul>
     * 
     * <p>Event Publishing:</p>
     * When book status changes from "Borrowed" to "Available", publishes a 
     * BookStatusEvent to Redis Stream for asynchronous wishlist notification processing.
     */
    @Override
    public ResponseDTO updateBook(BookDTO bookDTO) {

        validateBookDetails(bookDTO);

        BooksRecord bookRecord = bookRepository.fetchBookById(bookDTO.getId());
        BooksAvailabilitystatus oldStatus = bookRecord.getAvailabilitystatus();

        if(ObjectUtils.isEmpty(bookRecord)){
            throw new BookNotFoundException(bookDTO.getId());
        }

        if(!isUpdateRequired(bookDTO, bookRecord)){
            return ResponseDTO.builder()
                    .id(bookDTO.getId())
                    .message(NO_UPDATE_REQUIRED.getMessage())
                    .build();
        }

        bookRepository.save(mapToBookRecord(bookDTO, bookRecord));

        if(oldStatus!=bookDTO.getAvailabilityStatus()
                && bookDTO.getAvailabilityStatus()==BooksAvailabilitystatus.Available){

            BookStatusEvent event = BookStatusEvent.create(
                    bookDTO.getId(),
                    bookRecord.getTitle(),
                    bookRecord.getAuthor()
            );

            bookEventPublisher.publishBookStatusEvent(event);
        }
        return ResponseDTO.builder()
                .id(bookDTO.getId())
                .message(UPDATE_SUCCESSFUL.getMessage())
                .build();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementation Details:</p>
     * <ul>
     *   <li>Validates search parameters (text length, pagination bounds)</li>
     *   <li>Delegates to repository layer with jOOQ-based full-text search</li>
     *   <li>Uses MySQL MATCH AGAINST for efficient text searching</li>
     *   <li>Automatically filters out soft-deleted books</li>
     *   <li>Constructs response with result count and pagination metadata</li>
     * </ul>
     * 
     * <p>Performance Considerations:</p>
     * <ul>
     *   <li>Leverages MySQL full-text indexes for fast search</li>
     *   <li>Limits result set size to prevent memory issues</li>
     *   <li>Uses optimized queries with proper JOIN conditions</li>
     * </ul>
     */
    @Override
    public BookSearchResponseDTO searchBooks(String searchText, Integer publishedYear, Integer limit, Integer offset) {
        validateSearchInputs(searchText, publishedYear);
        Set<BookDTO> bookDTOSet = bookRepository.searchBooks(searchText, publishedYear, limit, offset);
        return BookSearchResponseDTO.builder()
                .totalCount(bookDTOSet.size())
                .books(bookDTOSet)
                .build();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementation Details:</p>
     * <ul>
     *   <li>Validates book existence by fetching from database</li>
     *   <li>Enforces business rule: borrowed books cannot be deleted</li>
     *   <li>Performs soft delete by setting isDeleted flag</li>
     *   <li>Maintains referential integrity for audit trails</li>
     * </ul>
     * 
     * <p>Soft Delete Strategy:</p>
     * This implementation uses soft deletion to preserve historical data and
     * maintain referential integrity with related entities like wishlists and
     * borrowing records.
     */
    @Override
    public ResponseDTO deleteBook(Long bookId) {
        BooksRecord bookRecord = bookRepository.fetchBookById(bookId);
        if (ObjectUtils.isEmpty(bookRecord)) {
            throw new BookNotFoundException(bookId);
        }
        bookRepository.delete(bookId);
        return ResponseDTO.builder()
                .id(bookId)
                .message(DELETE_SUCCESSFUL.getMessage())
                .build();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementation Details:</p>
     * <ul>
     *   <li>Enforces batch size limit of 100 books for performance</li>
     *   <li>Fetches all requested books in single database query</li>
     *   <li>Categorizes each book ID as deletable or not deletable</li>
     *   <li>Provides detailed failure reasons for debugging</li>
     *   <li>Performs batch soft delete for eligible books</li>
     * </ul>
     * 
     * <p>Business Logic Processing:</p>
     * <ul>
     *   <li>Non-existent books: Added to notDeletedBookIds with reason "Book does not exist"</li>
     *   <li>Borrowed books: Added to notDeletedBookIds with reason "Book is currently borrowed"</li>
     *   <li>Valid books: Added to deletedBookIds and processed for deletion</li>
     * </ul>
     * 
     * <p>Performance Optimization:</p>
     * Uses single batch query to fetch all books and single batch operation
     * for deletion, minimizing database round trips.
     */
    @Override
    public DeleteBooksResponseDTO deleteBook(Set<Long> bookIds) {
        if (bookIds.size() > 100) {
            throw new IllegalArgumentException("Cannot delete more than " + 100 + " books at once");
        }

        DeleteBooksResponseDTO response = new DeleteBooksResponseDTO();

        // Fetch existing books from DB
        Map<Long, BooksRecord> existingBooks = bookRepository.fetchBooksByIds(bookIds)
                .stream()
                .collect(Collectors.toMap(BooksRecord::getId, Function.identity()));

        for (Long id : bookIds) {
            BooksRecord book = existingBooks.get(id);
            if (book == null) {
                response.getNotDeletedBookIds().add(id);
                response.getReasons().put(id, "Book does not exist");
            } else if (book.getAvailabilitystatus() == BooksAvailabilitystatus.Borrowed) {
                response.getNotDeletedBookIds().add(id);
                response.getReasons().put(id, "Book is currently borrowed");
            } else {
                response.getDeletedBookIds().add(id);
            }
        }

        // Perform batch delete only for deletable books
        if (!response.getDeletedBookIds().isEmpty()) {
            bookRepository.removeBooks(new HashSet<>(response.getDeletedBookIds()));
        }

        return response;
    }

    /**
     * Validates search input parameters for book search operations.
     * 
     * <p>Validation Rules:</p>
     * <ul>
     *   <li>Search text must be between 3 and 255 characters</li>
     *   <li>Published year must be valid if provided</li>
     *   <li>Trims whitespace from search text</li>
     * </ul>
     * 
     * @param searchText the text to search for in book titles and authors
     * @param publishedYear optional year filter for book publication
     * @throws IllegalArgumentException if search text length is invalid
     * @throws InvalidBookDataException if published year is invalid
     */
    private void validateSearchInputs(String searchText, Integer publishedYear) {
        validatePublishedYear(publishedYear);

        if (searchText != null) {
            String trimmed = searchText.trim();
            int length = trimmed.length();

            if (length > 0 && length < 3) {
                throw new IllegalArgumentException("Search text must be at least 3 characters");
            }
            if (length > 255) {
                throw new IllegalArgumentException("Search text must be less than 255 characters");
            }
        }
    }

    /**
     * Validates book details for create and update operations.
     * 
     * <p>Validation Rules:</p>
     * <ul>
     *   <li>Published year must be within valid range (1450 - current year)</li>
     *   <li>ISBN must be unique in the system</li>
     * </ul>
     * 
     * @param bookDTO the book data to validate
     * @throws InvalidBookDataException if published year is invalid
     * @throws DuplicateBookException if ISBN already exists in the system
     */
    private void validateBookDetails(BookDTO bookDTO) {
        validatePublishedYear(bookDTO.getPublishedYear());
        if(bookRepository.isbnExists(bookDTO.getIsbn())) {
            throw new DuplicateBookException(bookDTO.getIsbn());
        }
    }

    /**
     * Validates the published year of a book.
     * 
     * <p>Valid Range:</p>
     * <ul>
     *   <li>Minimum: 1450 (approximate invention of printing press)</li>
     *   <li>Maximum: Current year (books cannot be published in the future)</li>
     * </ul>
     * 
     * @param publishedYear the year to validate
     * @throws InvalidBookDataException if the year is outside the valid range
     */
    private void validatePublishedYear(Integer publishedYear){
        if(!ObjectUtils.isEmpty(publishedYear) && (publishedYear < 1450 || publishedYear > Year.now().getValue())) {
            throw new InvalidBookDataException(String.valueOf(publishedYear), "invalid year");
        }
    }

    private boolean isUpdateRequired(BookDTO dto, BooksRecord record) {
        return !Objects.equals(dto.getTitle(), record.getTitle()) ||
                !Objects.equals(dto.getAuthor(), record.getAuthor()) ||
                !Objects.equals(dto.getIsbn(), record.getIsbn()) ||
                !Objects.equals(dto.getPublishedYear(), record.getPublishedyear().getValue()) ||
                !Objects.equals(dto.getAvailabilityStatus(), record.getAvailabilitystatus());
    }

    private BooksRecord mapToBookRecord(BookDTO bookDTO, BooksRecord bookRecord){
        if (bookDTO.getTitle() != null) bookRecord.setTitle(bookDTO.getTitle());
        if (bookDTO.getAuthor() != null) bookRecord.setAuthor(bookDTO.getAuthor());
        if (bookDTO.getIsbn() != null) bookRecord.setIsbn(bookDTO.getIsbn());
        if (bookDTO.getPublishedYear() != null) bookRecord.setPublishedyear(Year.of(bookDTO.getPublishedYear()));
        if (bookDTO.getAvailabilityStatus() != null) bookRecord.setAvailabilitystatus(bookDTO.getAvailabilityStatus());
        return  bookRecord;
    }

}
