package com.bookstore.core.service;


import com.bookstore.repo.author.Author;
import com.bookstore.repo.author.AuthorRepository;
import com.bookstore.repo.book.Book;
import com.bookstore.repo.book.BookRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CrudBookService {

    @Autowired
    BookRepository bookRepository;
    @Autowired
    AuthorRepository authorRepository;
    @Autowired
    ObjectMapper objectMapper;

    public ResponseEntity<String> createBookRecord (Book book) {
        try {
            Book newBook = new Book();
            Set<Author> authors = new HashSet<>();
            newBook.setIsbn(book.getIsbn());
            newBook.setYear(book.getYear());
            newBook.setTitle(book.getTitle());
            newBook.setPrice(book.getPrice());
            newBook.setGenre(book.getGenre());

            for (Author author : book.getAuthors()) {
                if (authorRepository.findByName(author.getName()) != null ) {
                    newBook.getAuthors().add(author);
                } else {
                    Author newAuthor = new Author();
                    newAuthor.setName(author.getName());
                    newAuthor.setBirthDate(author.getBirthDate());
                    newAuthor.getBooks().add(newBook);
                    authors.add(newAuthor);
                }
            }
            newBook.setAuthors(authors);
            if (!authors.isEmpty())
                authorRepository.saveAll(authors);
            bookRepository.save(newBook);


        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable To Create Record due to: " + ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Record Successfully Created");
    }

    public ResponseEntity<String> updateBookRecord (Book updatedBook, String isbn) {
        try {
            Optional<Book> bookOpt = bookRepository.findById(isbn);
            if (bookOpt.isPresent()) {
                Set<Author> authors = new HashSet<>();
                Book oldBook = bookOpt.get();
                oldBook.setTitle(updatedBook.getTitle());
                oldBook.setYear(updatedBook.getYear());
                oldBook.setPrice(updatedBook.getPrice());
                oldBook.setGenre(updatedBook.getGenre());

                for (Author author : updatedBook.getAuthors()) {
                    if (authorRepository.findByName(author.getName()) == null ) {
                        Author newAuthor = new Author();
                        newAuthor.setName(author.getName());
                        newAuthor.setBirthDate(author.getBirthDate());
                        newAuthor.getBooks().add(oldBook);
                        oldBook.getAuthors().add(author);
                        authorRepository.save(author);
                    } else {
                        Author existingAuthor = authorRepository.findByName(author.getName());
                        oldBook.getAuthors().add(existingAuthor);
                    }
                }
                bookRepository.save(oldBook);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable To Find Record: " + isbn);
            }
        } catch(Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable To Update Record due to: " + ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Record Successfully Updated");
    }

    public ResponseEntity<String> findByTitleOrAuthor(String title, String author) throws JsonProcessingException {
        List<Book> bookList = new ArrayList<>();
        try {
            if (title != null && author != null) {
                bookList = bookRepository.findByTitle(title).stream().filter(book -> book.getAuthors().stream().anyMatch(a -> a.getName().equalsIgnoreCase(author))).collect(Collectors.toList());
            } else if (author == null) {
                bookList = bookRepository.findByTitle(title);
            } else
                bookList = bookRepository.findByAuthorName(author);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable To Find Record due to: " + ex.getMessage());
        }
        String bookListJson = objectMapper.writeValueAsString(bookList);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(bookListJson);
    }

    public ResponseEntity<String> deleteBookRecord(String isbn) {
        try {
            bookRepository.deleteById(isbn);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable To Delete Record due to: " + ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Record Successfully Deleted");
    }


}
