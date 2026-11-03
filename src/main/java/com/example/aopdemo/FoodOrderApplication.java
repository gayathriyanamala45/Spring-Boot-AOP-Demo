package com.example.aopdemo;


import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.*;

@SpringBootApplication
public class FoodOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodOrderApplication.class, args);
        System.out.println("Online Food Order System Started Successfully!");
    }

    // MODEL
    @Entity
    @Table(name = "food_orders")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class FoodOrder {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long orderId;

        @NotBlank(message = "Customer name is required")
        @Column(nullable = false)
        private String customerName;

        @NotBlank(message = "Food item cannot be empty")
        @Column(nullable = false)
        private String foodItem;

        @Min(value = 1, message = "Quantity must be at least 1")
        private int quantity;

        @NotNull(message = "Price is mandatory")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        private Double price;
    }

    // REPOSITORY
    interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
    }

    // SERVICE
    @Service
    static class FoodOrderService {

        private final FoodOrderRepository repository;

        FoodOrderService(FoodOrderRepository repository) {
            this.repository = repository;
        }

        // GET ALL ORDERS
        public List<FoodOrder> getAllOrders() {
            return repository.findAll();
        }

        // GET ORDER BY ID
        public FoodOrder getOrderById(Long id) {
            return repository.findById(id)
                    .orElseThrow(() ->
                            new RuntimeException("Order not found: " + id));
        }

        // POST ADD ORDER
        public FoodOrder addOrder(FoodOrder order) {
            order.setOrderId(null);
            return repository.save(order);
        }

        // PUT UPDATE ORDER
        public FoodOrder updateOrder(Long id, FoodOrder newOrder) {

            FoodOrder order = getOrderById(id);

            order.setCustomerName(newOrder.getCustomerName());
            order.setFoodItem(newOrder.getFoodItem());
            order.setQuantity(newOrder.getQuantity());
            order.setPrice(newOrder.getPrice());

            return repository.save(order);
        }

        // PATCH PARTIAL UPDATE
        public FoodOrder patchOrder(Long id, FoodOrder updatedOrder) {

            FoodOrder order = getOrderById(id);

            if (updatedOrder.getCustomerName() != null)
                order.setCustomerName(updatedOrder.getCustomerName());

            if (updatedOrder.getFoodItem() != null)
                order.setFoodItem(updatedOrder.getFoodItem());

            if (updatedOrder.getQuantity() != 0)
                order.setQuantity(updatedOrder.getQuantity());

            if (updatedOrder.getPrice() != null)
                order.setPrice(updatedOrder.getPrice());

            return repository.save(order);
        }

        // DELETE ORDER
        public void deleteOrder(Long id) {
            repository.delete(getOrderById(id));
        }
    }

    // REST CONTROLLER
    @RestController
    @RequestMapping("/api/orders")
    static class FoodOrderController {

        private final FoodOrderService service;

        FoodOrderController(FoodOrderService service) {
            this.service = service;
        }

        // GET ALL
        @GetMapping
        public ResponseEntity<List<FoodOrder>> getAllOrders() {
            return ResponseEntity.ok(service.getAllOrders());
        }

        // GET BY ID
        @GetMapping("/{id}")
        public ResponseEntity<FoodOrder> getOrderById(@PathVariable Long id) {
            return ResponseEntity.ok(service.getOrderById(id));
        }

        // POST
        @PostMapping
        public ResponseEntity<FoodOrder> addOrder(
                @Valid @RequestBody FoodOrder order) {

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(service.addOrder(order));
        }

        // PUT
        @PutMapping("/{id}")
        public ResponseEntity<FoodOrder> updateOrder(
                @PathVariable Long id,
                @Valid @RequestBody FoodOrder order) {

            return ResponseEntity.ok(service.updateOrder(id, order));
        }

        // PATCH
        @PatchMapping("/{id}")
        public ResponseEntity<FoodOrder> patchOrder(
                @PathVariable Long id,
                @RequestBody FoodOrder order) {

            return ResponseEntity.ok(service.patchOrder(id, order));
        }

        // DELETE
        @DeleteMapping("/{id}")
        public ResponseEntity<String> deleteOrder(@PathVariable Long id) {

            service.deleteOrder(id);

            return ResponseEntity.ok("Order deleted successfully!");
        }
    }

    // GLOBAL EXCEPTION HANDLER
    @RestControllerAdvice
    static class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, String>> handleValidation(
                MethodArgumentNotValidException ex) {

            Map<String, String> errors = new HashMap<>();

            ex.getBindingResult().getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));

            return ResponseEntity.badRequest().body(errors);
        }

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<Map<String, String>> handleException(
                RuntimeException ex) {

            Map<String, String> error = new HashMap<>();
            error.put("error", ex.getMessage());

            HttpStatus status = ex.getMessage().startsWith("Order not found")
                    ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status).body(error);
        }
    }
}