package com.orders.controller;

import com.orders.client.dto.UserClientResponse;
import com.orders.dto.ApiResponse;
import com.orders.dto.OrderCreateRequest;
import com.orders.dto.OrderResponse;
import com.orders.dto.OrderUpdateRequest;
import com.orders.dto.OrderWithUserResponse;
import com.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId) {
        List<OrderResponse> orders;
        if (userId != null && status != null && !status.isBlank()) {
            orders = orderService.getOrdersByUserIdAndStatus(userId, status);
        } else if (userId != null) {
            orders = orderService.getOrdersByUserId(userId);
        } else if (status != null && !status.isBlank()) {
            orders = orderService.getOrdersByStatus(status);
        } else {
            orders = orderService.getAllOrders();
        }
        return ResponseEntity.ok(ApiResponse.ok("Orders retrieved successfully", orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.ok("Order retrieved successfully", order));
    }

    @GetMapping("/order-number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        OrderResponse order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.ok("Order retrieved successfully", order));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUserId(@PathVariable Long userId) {
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok("User orders retrieved successfully", orders));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> searchOrders(
            @RequestParam String query) {
        List<OrderResponse> orders = orderService.searchOrders(query);
        return ResponseEntity.ok(ApiResponse.ok("Search results retrieved successfully", orders));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request) {
        OrderResponse createdOrder = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order created successfully", createdOrder));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderUpdateRequest request) {
        OrderResponse updatedOrder = orderService.updateOrder(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Order updated successfully", updatedOrder));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        OrderResponse updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Order status updated successfully", updatedOrder));
    }

    @GetMapping("/{id}/with-user")
    public ResponseEntity<ApiResponse<OrderWithUserResponse>> getOrderWithUserDetails(@PathVariable Long id) {
        OrderWithUserResponse orderWithUser = orderService.getOrderWithUserDetails(id);
        return ResponseEntity.ok(ApiResponse.ok("Order with user details retrieved successfully", orderWithUser));
    }

    @GetMapping("/{id}/user")
    public ResponseEntity<ApiResponse<UserClientResponse>> getUserForOrder(@PathVariable Long id) {
        UserClientResponse user = orderService.getUserForOrder(id);
        return ResponseEntity.ok(ApiResponse.ok("User retrieved successfully for order", user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.ok("Order deleted successfully", null));
    }
}
