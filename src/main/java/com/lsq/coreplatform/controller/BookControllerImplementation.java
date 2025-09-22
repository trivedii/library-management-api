package com.lsq.coreplatform.controller;

import com.lsq.coreplatform.dto.request.BookDTO;
import com.lsq.coreplatform.dto.response.BookSearchResponseDTO;
import com.lsq.coreplatform.dto.response.DeleteBooksResponseDTO;
import com.lsq.coreplatform.dto.response.ResponseDTO;
import com.lsq.coreplatform.service.BookSerivce;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/library/books")
@Validated
public class BookControllerImplementation implements BookController {

    @Autowired
    private BookSerivce bookSerivce;

    @PostMapping()
    public ResponseEntity<ResponseDTO> addBook(@Valid @RequestBody BookDTO bookDTO) {
        return ResponseEntity.ok(bookSerivce.saveBook(bookDTO));
    }

    @PatchMapping()
    public ResponseEntity<ResponseDTO> updateBook(@Valid @RequestBody BookDTO bookDTO) {
        return  ResponseEntity.ok(bookSerivce.updateBook(bookDTO));
    }

    @GetMapping("/search")
    public ResponseEntity<BookSearchResponseDTO> searchBooks(@RequestParam(required = false) String searchText,
                                                             @RequestParam(required = false) Integer publishedYear,
                                                             @RequestParam(defaultValue = "25") @Min(1) @Max(100) Integer limit,
                                                             @RequestParam(defaultValue = "0") @Min(0) Integer offset) {
       return  ResponseEntity.ok(bookSerivce.searchBooks(searchText, publishedYear, limit, offset));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<ResponseDTO> deleteBook(@PathVariable Long bookId){
        return  ResponseEntity.ok(bookSerivce.deleteBook(bookId));
    }

    @DeleteMapping("/delete-batch")
    public ResponseEntity<DeleteBooksResponseDTO> deleteBooksInBatch(@RequestBody Set<Long> bookIds){
        return ResponseEntity.ok(bookSerivce.deleteBook(bookIds));
    }
}
