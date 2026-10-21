package com.orders.client.config;

import com.orders.client.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = "users", types = {UsersService.class})
@Slf4j
public class HttpServiceClientConfig {

    @Value("${users.service.url:http://localhost:8082/api/users}")
    private String usersServiceUrl;

    @Bean
    public RestClientHttpServiceGroupConfigurer groupConfigurer() {
        return groups -> {
            groups.filterByName("users").forEachClient((group, restClient) -> {
                restClient.baseUrl(usersServiceUrl)
                        .requestInterceptor((request, body, execution) -> {
                            log.debug("HTTP Exchange Client sending {} request to {}", request.getMethod(), request.getURI());
                            return execution.execute(request, body);
                        });
            });
        };
    }
}
