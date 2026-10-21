package com.orders.controller;

import com.orders.client.dto.UserClientResponse;
import com.orders.dto.*;
import com.orders.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private OrderResponse sampleOrderResponse;
    private UserClientResponse sampleUserResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();

        sampleOrderResponse = OrderResponse.builder()
                .id(1L)
                .orderNumber("ORD-2026-0001")
                .userId(1L)
                .customerEmail("alice.smith@example.com")
                .shippingAddress("123 Maple Street")
                .paymentMethod("CREDIT_CARD")
                .status("PENDING")
                .paymentStatus("PENDING")
                .totalAmount(new BigDecimal("99.99"))
                .items(Collections.emptyList())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleUserResponse = UserClientResponse.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith@example.com")
                .role("ADMIN")
                .build();
    }

    @Test
    void shouldGetAllOrders() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(sampleOrderResponse));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].orderNumber").value("ORD-2026-0001"));
    }

    @Test
    void shouldGetOrderById() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(sampleOrderResponse);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void shouldGetOrderByOrderNumber() throws Exception {
        when(orderService.getOrderByOrderNumber("ORD-2026-0001")).thenReturn(sampleOrderResponse);

        mockMvc.perform(get("/api/orders/order-number/ORD-2026-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-2026-0001"));
    }

    @Test
    void shouldGetOrdersByUserId() throws Exception {
        when(orderService.getOrdersByUserId(1L)).thenReturn(List.of(sampleOrderResponse));

        mockMvc.perform(get("/api/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userId").value(1));
    }

    @Test
    void shouldCreateOrder() throws Exception {
        String requestJson = """
                {
                    "userId": 1,
                    "customerEmail": "alice.smith@example.com",
                    "shippingAddress": "123 Maple Street",
                    "paymentMethod": "CREDIT_CARD",
                    "items": [
                        {
                            "productName": "Keyboard",
                            "quantity": 1,
                            "unitPrice": 99.99
                        }
                    ]
                }
                """;

        when(orderService.createOrder(any(OrderCreateRequest.class))).thenReturn(sampleOrderResponse);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-2026-0001"));
    }

    @Test
    void shouldUpdateOrder() throws Exception {
        String updateJson = """
                {
                    "status": "SHIPPED"
                }
                """;

        when(orderService.updateOrder(eq(1L), any(OrderUpdateRequest.class))).thenReturn(sampleOrderResponse);

        mockMvc.perform(put("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldUpdateOrderStatus() throws Exception {
        when(orderService.updateOrderStatus(1L, "DELIVERED")).thenReturn(sampleOrderResponse);

        mockMvc.perform(patch("/api/orders/1/status")
                        .param("status", "DELIVERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldGetOrderWithUserDetails() throws Exception {
        OrderWithUserResponse orderWithUser = OrderWithUserResponse.builder()
                .order(sampleOrderResponse)
                .user(sampleUserResponse)
                .build();

        when(orderService.getOrderWithUserDetails(1L)).thenReturn(orderWithUser);

        mockMvc.perform(get("/api/orders/1/with-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.order.orderNumber").value("ORD-2026-0001"))
                .andExpect(jsonPath("$.data.user.firstName").value("Alice"));
    }

    @Test
    void shouldGetUserForOrder() throws Exception {
        when(orderService.getUserForOrder(1L)).thenReturn(sampleUserResponse);

        mockMvc.perform(get("/api/orders/1/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Alice"))
                .andExpect(jsonPath("$.data.email").value("alice.smith@example.com"));
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        doNothing().when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
