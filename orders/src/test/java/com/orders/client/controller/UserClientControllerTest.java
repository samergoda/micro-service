package com.orders.client.controller;

import com.orders.client.dto.UserClientResponse;
import com.orders.client.service.UsersService;
import com.orders.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserClientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsersService usersService;

    @InjectMocks
    private UserClientController userClientController;

    private UserClientResponse sampleUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userClientController).build();

        sampleUser = UserClientResponse.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith@example.com")
                .role("ADMIN")
                .build();
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        when(usersService.getAllUsers(null)).thenReturn(ApiResponse.ok(List.of(sampleUser)));

        mockMvc.perform(get("/api/orders/client/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].email").value("alice.smith@example.com"));
    }

    @Test
    void shouldGetUserById() throws Exception {
        when(usersService.getUserById(1L)).thenReturn(ApiResponse.ok(sampleUser));

        mockMvc.perform(get("/api/orders/client/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Alice"));
    }

    @Test
    void shouldGetUserByEmail() throws Exception {
        when(usersService.getUserByEmail("alice.smith@example.com")).thenReturn(ApiResponse.ok(sampleUser));

        mockMvc.perform(get("/api/orders/client/users/email/alice.smith@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("alice.smith@example.com"));
    }

    @Test
    void shouldSearchUsers() throws Exception {
        when(usersService.searchUsers("Alice")).thenReturn(ApiResponse.ok(List.of(sampleUser)));

        mockMvc.perform(get("/api/orders/client/users/search").param("query", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].firstName").value("Alice"));
    }
}
