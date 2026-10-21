package com.orders.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderUpdateRequest {

    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;

    @Size(max = 50, message = "Payment status must not exceed 50 characters")
    private String paymentStatus;

    @Size(max = 255, message = "Shipping address must not exceed 255 characters")
    private String shippingAddress;

    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;
}
