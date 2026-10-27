package com.orders.service.impl;

import com.orders.client.dto.UserClientResponse;
import com.orders.client.service.UsersService;
import com.orders.dto.*;
import com.orders.entity.Order;
import com.orders.entity.OrderItem;
import com.orders.event.OrderCreatedEvent;
import com.orders.event.OrderEventPublisher;
import com.orders.exception.OrderAlreadyExistsException;
import com.orders.exception.ResourceNotFoundException;
import com.orders.repository.OrderRepository;
import com.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UsersService usersService;
    private final OrderEventPublisher orderEventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return OrderResponse.fromEntity(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        return OrderResponse.fromEntity(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(String status) {
        return orderRepository.findByStatusIgnoreCase(status)
                .stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserIdAndStatus(Long userId, String status) {
        return orderRepository.findByUserIdAndStatusIgnoreCase(userId, status)
                .stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @Override
    public OrderResponse createOrder(OrderCreateRequest request) {
        // Validate user existence via UsersService HTTP Exchange client if available
        try {
            ApiResponse<UserClientResponse> userResponse = usersService.getUserById(request.getUserId());
            if (userResponse != null && userResponse.getData() != null) {
                log.info("Verified customer from UsersService: {} {}",
                        userResponse.getData().getFirstName(), userResponse.getData().getLastName());
            }
        } catch (Exception ex) {
            log.warn("Could not verify user {} via UsersService: {}", request.getUserId(), ex.getMessage());
        }

        String orderNumber = request.getOrderNumber();
        if (orderNumber == null || orderNumber.isBlank()) {
            orderNumber = generateOrderNumber();
        } else if (orderRepository.existsByOrderNumber(orderNumber)) {
            throw new OrderAlreadyExistsException("Order number already exists: " + orderNumber);
        }

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(request.getUserId())
                .customerEmail(request.getCustomerEmail())
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .status(request.getStatus() != null && !request.getStatus().isBlank()
                        ? request.getStatus().toUpperCase()
                        : "PENDING")
                .paymentStatus(request.getPaymentStatus() != null && !request.getPaymentStatus().isBlank()
                        ? request.getPaymentStatus().toUpperCase()
                        : "PENDING")
                .items(new ArrayList<>())
                .build();

        BigDecimal computedTotal = BigDecimal.ZERO;
        if (request.getItems() != null) {
            for (OrderItemRequest itemReq : request.getItems()) {
                BigDecimal subtotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
                computedTotal = computedTotal.add(subtotal);

                OrderItem item = OrderItem.builder()
                        .productName(itemReq.getProductName())
                        .productSku(itemReq.getProductSku())
                        .quantity(itemReq.getQuantity())
                        .unitPrice(itemReq.getUnitPrice())
                        .subtotal(subtotal)
                        .build();

                order.addItem(item);
            }
        }

        order.setTotalAmount(request.getTotalAmount() != null ? request.getTotalAmount() : computedTotal);

        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                "ORDER_CREATED",
                summarizeProducts(savedOrder)
        );

        orderEventPublisher.publishOrderCreated(event);

        return OrderResponse.fromEntity(savedOrder);
    }

    /**
     * Builds a human-readable product summary for the order-created event,
     * e.g. "Widget" or "Widget (+2 more)". Returns null when there are no items.
     */
    private String summarizeProducts(Order order) {
        List<OrderItem> items = order.getItems();
        if (items == null || items.isEmpty()) {
            return null;
        }
        String first = items.get(0).getProductName();
        if (items.size() == 1) {
            return first;
        }
        return first + " (+" + (items.size() - 1) + " more)";
    }

    @Override
    public OrderResponse updateOrder(Long id, OrderUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            order.setStatus(request.getStatus().toUpperCase());
        }

        if (request.getPaymentStatus() != null && !request.getPaymentStatus().isBlank()) {
            order.setPaymentStatus(request.getPaymentStatus().toUpperCase());
        }

        if (request.getShippingAddress() != null && !request.getShippingAddress().isBlank()) {
            order.setShippingAddress(request.getShippingAddress());
        }

        if (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank()) {
            order.setPaymentMethod(request.getPaymentMethod());
        }

        Order updatedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(updatedOrder);
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        order.setStatus(status.toUpperCase());
        Order updatedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(updatedOrder);
    }

    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> searchOrders(String query) {
        if (query == null || query.isBlank()) {
            return getAllOrders();
        }
        return orderRepository.searchOrders(query)
                .stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderWithUserResponse getOrderWithUserDetails(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        UserClientResponse user = null;
        try {
            ApiResponse<UserClientResponse> userApiResponse = usersService.getUserById(order.getUserId());
            if (userApiResponse != null) {
                user = userApiResponse.getData();
            }
        } catch (Exception ex) {
            log.warn("Failed to fetch user details for user id {}: {}", order.getUserId(), ex.getMessage());
        }

        return OrderWithUserResponse.builder()
                .order(OrderResponse.fromEntity(order))
                .user(user)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserClientResponse getUserForOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        try {
            ApiResponse<UserClientResponse> response = usersService.getUserById(order.getUserId());
            if (response != null && response.getData() != null) {
                return response.getData();
            }
            throw new ResourceNotFoundException("User not found in Users service with id: " + order.getUserId());
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Error communicating with Users service: " + ex.getMessage(), ex);
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
