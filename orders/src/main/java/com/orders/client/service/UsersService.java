package com.orders.client.service;

import com.orders.client.dto.UserClientResponse;
import com.orders.dto.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange
public interface UsersService {

    @GetExchange
    ApiResponse<List<UserClientResponse>> getAllUsers(@RequestParam(value = "role", required = false) String role);

    @GetExchange("/{id}")
    ApiResponse<UserClientResponse> getUserById(@PathVariable("id") Long id);

    @GetExchange("/email/{email}")
    ApiResponse<UserClientResponse> getUserByEmail(@PathVariable("email") String email);

    @GetExchange("/search")
    ApiResponse<List<UserClientResponse>> searchUsers(@RequestParam("query") String query);
}
