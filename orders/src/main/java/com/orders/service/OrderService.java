package com.orders.service;

import com.orders.client.dto.UserClientResponse;
import com.orders.dto.OrderCreateRequest;
import com.orders.dto.OrderResponse;
import com.orders.dto.OrderUpdateRequest;
import com.orders.dto.OrderWithUserResponse;

import java.util.List;

public interface OrderService {

    List<OrderResponse> getAllOrders();

    OrderResponse getOrderById(Long id);

    OrderResponse getOrderByOrderNumber(String orderNumber);

    List<OrderResponse> getOrdersByUserId(Long userId);

    List<OrderResponse> getOrdersByStatus(String status);

    List<OrderResponse> getOrdersByUserIdAndStatus(Long userId, String status);

    OrderResponse createOrder(OrderCreateRequest request);

    OrderResponse updateOrder(Long id, OrderUpdateRequest request);

    OrderResponse updateOrderStatus(Long id, String status);

    void deleteOrder(Long id);

    List<OrderResponse> searchOrders(String query);

    OrderWithUserResponse getOrderWithUserDetails(Long id);

    UserClientResponse getUserForOrder(Long id);
}
