package com.orders.service;

import com.orders.client.dto.UserClientResponse;
import com.orders.client.service.UsersService;
import com.orders.dto.*;
import com.orders.entity.Order;
import com.orders.entity.OrderItem;
import com.orders.exception.OrderAlreadyExistsException;
import com.orders.exception.ResourceNotFoundException;
import com.orders.repository.OrderRepository;
import com.orders.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UsersService usersService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        OrderItem item = OrderItem.builder()
                .id(1L)
                .productName("Mechanical Keyboard")
                .productSku("SKU-KEY-01")
                .quantity(1)
                .unitPrice(new BigDecimal("99.99"))
                .subtotal(new BigDecimal("99.99"))
                .build();

        sampleOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-2026-0001")
                .userId(1L)
                .customerEmail("alice.smith@example.com")
                .shippingAddress("123 Maple Street")
                .paymentMethod("CREDIT_CARD")
                .status("PENDING")
                .paymentStatus("PENDING")
                .totalAmount(new BigDecimal("99.99"))
                .items(new ArrayList<>(List.of(item)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        item.setOrder(sampleOrder);
    }

    @Test
    void shouldReturnAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(sampleOrder));

        List<OrderResponse> result = orderService.getAllOrders();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderNumber()).isEqualTo("ORD-2026-0001");
        assertThat(result.get(0).getCustomerEmail()).isEqualTo("alice.smith@example.com");
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void shouldGetOrderByIdWhenFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        OrderResponse result = orderService.getOrderById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOrderNumber()).isEqualTo("ORD-2026-0001");
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFoundById() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found with id: 99");
    }

    @Test
    void shouldGetOrderByOrderNumberWhenFound() {
        when(orderRepository.findByOrderNumber("ORD-2026-0001")).thenReturn(Optional.of(sampleOrder));

        OrderResponse result = orderService.getOrderByOrderNumber("ORD-2026-0001");

        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo("ORD-2026-0001");
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFoundByOrderNumber() {
        when(orderRepository.findByOrderNumber("ORD-NON-EXISTENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderByOrderNumber("ORD-NON-EXISTENT"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found with order number: ORD-NON-EXISTENT");
    }

    @Test
    void shouldGetOrdersByUserId() {
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(sampleOrder));

        List<OrderResponse> result = orderService.getOrdersByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void shouldGetOrdersByStatus() {
        when(orderRepository.findByStatusIgnoreCase("PENDING")).thenReturn(List.of(sampleOrder));

        List<OrderResponse> result = orderService.getOrdersByStatus("PENDING");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldGetOrdersByUserIdAndStatus() {
        when(orderRepository.findByUserIdAndStatusIgnoreCase(1L, "PENDING")).thenReturn(List.of(sampleOrder));

        List<OrderResponse> result = orderService.getOrdersByUserIdAndStatus(1L, "PENDING");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldCreateOrderSuccessfullyWithCalculatedTotal() {
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productName("Wireless Mouse")
                .productSku("SKU-MOU-01")
                .quantity(2)
                .unitPrice(new BigDecimal("25.00"))
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .orderNumber("ORD-CUSTOM-01")
                .userId(1L)
                .customerEmail("alice.smith@example.com")
                .shippingAddress("123 Maple Street")
                .paymentMethod("PAYPAL")
                .items(List.of(itemRequest))
                .build();

        UserClientResponse mockUser = UserClientResponse.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith@example.com")
                .build();

        when(usersService.getUserById(1L)).thenReturn(ApiResponse.ok(mockUser));
        when(orderRepository.existsByOrderNumber("ORD-CUSTOM-01")).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(2L);
            return o;
        });

        OrderResponse result = orderService.createOrder(request);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getOrderNumber()).isEqualTo("ORD-CUSTOM-01");
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getSubtotal()).isEqualByComparingTo(new BigDecimal("50.00"));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingOrderWithExistingOrderNumber() {
        OrderCreateRequest request = OrderCreateRequest.builder()
                .orderNumber("ORD-2026-0001")
                .userId(1L)
                .customerEmail("alice.smith@example.com")
                .shippingAddress("123 Maple Street")
                .paymentMethod("CREDIT_CARD")
                .items(List.of(OrderItemRequest.builder()
                        .productName("Item")
                        .quantity(1)
                        .unitPrice(BigDecimal.TEN)
                        .build()))
                .build();

        when(orderRepository.existsByOrderNumber("ORD-2026-0001")).thenReturn(true);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(OrderAlreadyExistsException.class)
                .hasMessageContaining("Order number already exists");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldUpdateOrderSuccessfully() {
        OrderUpdateRequest updateRequest = OrderUpdateRequest.builder()
                .status("SHIPPED")
                .paymentStatus("PAID")
                .shippingAddress("New Address, Suite 100")
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse result = orderService.updateOrder(1L, updateRequest);

        assertThat(result.getStatus()).isEqualTo("SHIPPED");
        assertThat(result.getPaymentStatus()).isEqualTo("PAID");
        assertThat(result.getShippingAddress()).isEqualTo("New Address, Suite 100");
        verify(orderRepository, times(1)).save(sampleOrder);
    }

    @Test
    void shouldUpdateOrderStatusSuccessfully() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse result = orderService.updateOrderStatus(1L, "DELIVERED");

        assertThat(result.getStatus()).isEqualTo("DELIVERED");
        verify(orderRepository, times(1)).save(sampleOrder);
    }

    @Test
    void shouldDeleteOrderSuccessfully() {
        when(orderRepository.existsById(1L)).thenReturn(true);
        doNothing().when(orderRepository).deleteById(1L);

        orderService.deleteOrder(1L);

        verify(orderRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentOrder() {
        when(orderRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.deleteOrder(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found with id: 99");

        verify(orderRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldSearchOrdersByQuery() {
        when(orderRepository.searchOrders("alice")).thenReturn(List.of(sampleOrder));

        List<OrderResponse> result = orderService.searchOrders("alice");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerEmail()).isEqualTo("alice.smith@example.com");
    }

    @Test
    void shouldGetOrderWithUserDetails() {
        UserClientResponse mockUser = UserClientResponse.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith@example.com")
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(usersService.getUserById(1L)).thenReturn(ApiResponse.ok(mockUser));

        OrderWithUserResponse result = orderService.getOrderWithUserDetails(1L);

        assertThat(result).isNotNull();
        assertThat(result.getOrder().getOrderNumber()).isEqualTo("ORD-2026-0001");
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getFirstName()).isEqualTo("Alice");
    }

    @Test
    void shouldGetUserForOrder() {
        UserClientResponse mockUser = UserClientResponse.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith@example.com")
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(usersService.getUserById(1L)).thenReturn(ApiResponse.ok(mockUser));

        UserClientResponse result = orderService.getUserForOrder(1L);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Alice");
        assertThat(result.getEmail()).isEqualTo("alice.smith@example.com");
    }
}
