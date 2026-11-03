package com.example.aopdemo;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@SpringBootApplication
public class LibraryBookManagementSystem {

    public static void main(String[] args) {

        SpringApplication.run(LibraryBookManagementSystem.class, args);

        System.out.println("Library Book Management System Started Successfully!");
    }

    // MODEL CLASS
    static class Book {

        private Long bookId;

        @NotBlank(message = "Book name is required")
        private String bookName;

        @NotBlank(message = "Author name is required")
        private String author;

        @Positive(message = "Price must be positive")
        private double price;

        private boolean available;

        public Book() {
        }

        public Book(Long bookId, String bookName,
                    String author, double price, boolean available) {

            this.bookId = bookId;
            this.bookName = bookName;
            this.author = author;
            this.price = price;
            this.available = available;
        }

        public Long getBookId() {
            return bookId;
        }

        public void setBookId(Long bookId) {
            this.bookId = bookId;
        }

        public String getBookName() {
            return bookName;
        }

        public void setBookName(String bookName) {
            this.bookName = bookName;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }
    }

    // PATCH REQUEST CLASS
    static class AvailabilityRequest {

        @NotNull(message = "Availability status is required")
        private Boolean available;

        public Boolean getAvailable() {
            return available;
        }

        public void setAvailable(Boolean available) {
            this.available = available;
        }
    }

    // CUSTOM EXCEPTION
    static class BookNotFoundException extends RuntimeException {

        public BookNotFoundException(String message) {
            super(message);
        }
    }

    // SERVICE CLASS
    @Service
    static class BookService {

        private final Map<Long, Book> books = new ConcurrentHashMap<>();

        private final AtomicLong idGenerator = new AtomicLong(1);

        // ADD BOOK
        public Book addBook(Book book) {

            Long id = idGenerator.getAndIncrement();

            book.setBookId(id);

            books.put(id, book);

            return book;
        }

        // GET ALL BOOKS
        public List<Book> getAllBooks() {

            return new ArrayList<>(books.values());
        }

        // SEARCH BOOK BY ID
        public Book getBookById(Long id) {

            Book book = books.get(id);

            if (book == null) {
                throw new BookNotFoundException(
                    "Book not found with ID: " + id);
            }

            return book;
        }

        // UPDATE AVAILABILITY
        public Book updateAvailability(Long id, boolean available) {

            Book book = getBookById(id);

            book.setAvailable(available);

            return book;
        }

        // DELETE BOOK
        public String deleteBook(Long id) {

            Book book = getBookById(id);

            books.remove(id);

            return "Book deleted successfully!";
        }
    }

    // REST CONTROLLER
    @RestController
    @RequestMapping("/api/books")
    static class BookController {

        private final BookService service;

        BookController(BookService service) {
            this.service = service;
        }

        // GET ALL BOOKS
        @GetMapping
        public ResponseEntity<List<Book>> getAllBooks() {

            return ResponseEntity.ok(service.getAllBooks());
        }

        // GET BOOK BY ID
        @GetMapping("/{id}")
        public ResponseEntity<Book> getBookById(
                @PathVariable Long id) {

            return ResponseEntity.ok(service.getBookById(id));
        }

        // POST ADD BOOK
        @PostMapping
        public ResponseEntity<Book> addBook(
                @Valid @RequestBody Book book) {

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(service.addBook(book));
        }

        // PATCH UPDATE AVAILABILITY
        @PatchMapping("/{id}/availability")
        public ResponseEntity<Book> updateAvailability(
                @PathVariable Long id,
                @Valid @RequestBody AvailabilityRequest request) {

            return ResponseEntity.ok(
                    service.updateAvailability(
                            id, request.getAvailable()));
        }

        // DELETE BOOK
        @DeleteMapping("/{id}")
        public ResponseEntity<String> deleteBook(
                @PathVariable Long id) {

            return ResponseEntity.ok(service.deleteBook(id));
        }
    }

    // GLOBAL EXCEPTION HANDLER
    @RestControllerAdvice
    static class GlobalExceptionHandler {

        // VALIDATION ERRORS
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, String>> handleValidation(
                MethodArgumentNotValidException ex) {

            Map<String, String> errors = new ConcurrentHashMap<>();

            ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );

            return ResponseEntity.badRequest().body(errors);
        }

        // BOOK NOT FOUND
        @ExceptionHandler(BookNotFoundException.class)
        public ResponseEntity<Map<String, String>> handleNotFound(
                BookNotFoundException ex) {

            Map<String, String> error = new ConcurrentHashMap<>();

            error.put("error", ex.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(error);
        }
    }
}