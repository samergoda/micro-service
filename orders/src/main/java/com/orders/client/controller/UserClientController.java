package com.orders.client.controller;

import com.orders.client.dto.UserClientResponse;
import com.orders.client.service.UsersService;
import com.orders.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders/client/users")
@RequiredArgsConstructor
public class UserClientController {

    private final UsersService usersService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserClientResponse>>> getAllUsers(
            @RequestParam(required = false) String role) {
        ApiResponse<List<UserClientResponse>> response = usersService.getAllUsers(role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserClientResponse>> getUserById(@PathVariable Long id) {
        ApiResponse<UserClientResponse> response = usersService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserClientResponse>> getUserByEmail(@PathVariable String email) {
        ApiResponse<UserClientResponse> response = usersService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserClientResponse>>> searchUsers(@RequestParam String query) {
        ApiResponse<List<UserClientResponse>> response = usersService.searchUsers(query);
        return ResponseEntity.ok(response);
    }
}
