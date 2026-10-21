package com.orders.dto;

import com.orders.client.dto.UserClientResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderWithUserResponse {

    private OrderResponse order;
    private UserClientResponse user;
}
