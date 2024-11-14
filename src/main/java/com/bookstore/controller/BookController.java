package com.bookstore.controller;

import com.bookstore.core.service.CrudBookService;
import com.bookstore.repo.book.Book;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/books/")
public class BookController {

    @Autowired
    CrudBookService crudBookService;

    @PostMapping("create/record")
    public ResponseEntity<String> createRecord(@RequestBody Book book) {
        return crudBookService.createBookRecord(book);
    }

    @PostMapping("update/record/{isbn}")
    public ResponseEntity<String> updateRecord(@PathVariable String isbn, @RequestBody Book book) {
        return crudBookService.updateBookRecord(book,isbn);
    }

    @GetMapping("find/record")
    public ResponseEntity<String> findRecord(@RequestParam(required = false) String title, @RequestParam(required = false) String author) throws JsonProcessingException {
        if (title == null && author == null) {
            return new ResponseEntity<>("Invalid number of parameter", HttpStatus.BAD_REQUEST);
        } else
            return crudBookService.findByTitleOrAuthor(title,author);
    }

    @DeleteMapping("delete/record/{isbn}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteRecord(@PathVariable String isbn) {
        return crudBookService.deleteBookRecord(isbn);
    }
}
