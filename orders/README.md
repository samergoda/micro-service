# Orders Microservice

A Spring Boot 4 RESTful microservice for managing orders and order items in an e-commerce platform, integrated with the `users` microservice via Spring declarative `@HttpExchange` HTTP Interface clients.

---

## 🚀 Overview

- **Framework**: Spring Boot 4.1.1
- **Java Version**: Java 25
- **Database**: MySQL 8.0 (production/docker) & H2 (in-memory for automated tests)
- **Port**: `8081` (avoids conflict with `users` microservice on `8080`)
- **Microservice Client**: Spring `@HttpExchange` HTTP Client via `@ImportHttpServices` and `RestClientHttpServiceGroupConfigurer`

---

## 📋 REST API Endpoints

### 1. Order Endpoints (`/api/orders`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/orders` | List all orders (supports optional `?status=` and `?userId=`) |
| `GET` | `/api/orders/{id}` | Retrieve order by ID |
| `GET` | `/api/orders/{id}/with-user` | Retrieve order along with customer details from Users microservice |
| `GET` | `/api/orders/{id}/user` | Retrieve customer profile directly for a given order |
| `GET` | `/api/orders/order-number/{orderNumber}` | Retrieve order by unique order number |
| `GET` | `/api/orders/user/{userId}` | Retrieve all orders placed by a specific user |
| `GET` | `/api/orders/search?query={q}` | Search orders by number, email, or shipping address |
| `POST` | `/api/orders` | Create a new order with items |
| `PUT` | `/api/orders/{id}` | Update order details (address, status, payment method/status) |
| `PATCH` | `/api/orders/{id}/status?status={status}` | Update order status directly |
| `DELETE` | `/api/orders/{id}` | Delete an order |

### 2. Users Client Endpoints (`/api/orders/client/users`)
Directly proxies calls to the Users microservice via declarative `@HttpExchange`:

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/orders/client/users` | Retrieve users list from Users microservice |
| `GET` | `/api/orders/client/users/{id}` | Retrieve user by ID from Users microservice |
| `GET` | `/api/orders/client/users/email/{email}` | Retrieve user by email from Users microservice |
| `GET` | `/api/orders/client/users/search?query={q}` | Search users via Users microservice |

---

## 🌐 HTTP Exchange Client Configuration

The HTTP Client is configured using Spring's declarative `@HttpExchange`:

- **Interface**: [`UsersService`](src/main/java/com/orders/client/service/UsersService.java)
- **Config**: [`HttpServiceClientConfig`](src/main/java/com/orders/client/config/HttpServiceClientConfig.java) with `@ImportHttpServices(group = "users", types = {UsersService.class})`
- **Configurable URL**: `users.service.url=http://localhost:8080/api/users` in `application.properties`

---

## 📦 Sample Payloads

### 1. Create Order (`POST /api/orders`)

```json
{
  "orderNumber": "ORD-2026-9999",
  "userId": 1,
  "customerEmail": "alice.smith@example.com",
  "shippingAddress": "123 Maple Street, Springfield, IL 62701",
  "paymentMethod": "CREDIT_CARD",
  "status": "PENDING",
  "paymentStatus": "PENDING",
  "items": [
    {
      "productName": "Ergonomic Mechanical Keyboard",
      "productSku": "SKU-KEY-01",
      "quantity": 1,
      "unitPrice": 129.99
    },
    {
      "productName": "Wireless Gaming Mouse",
      "productSku": "SKU-MOU-02",
      "quantity": 1,
      "unitPrice": 59.99
    }
  ]
}
```

*Note: If `orderNumber` is omitted, one is automatically generated (e.g. `ORD-XXXXXXXX`). If `totalAmount` is omitted, it is automatically calculated as the sum of `unitPrice * quantity` across all items.*

### 2. Update Order (`PUT /api/orders/{id}`)

```json
{
  "status": "PROCESSING",
  "paymentStatus": "PAID",
  "shippingAddress": "789 Pine Road, Chicago, IL 60601",
  "paymentMethod": "CREDIT_CARD"
}
```

---

## 🛠️ Running the Application

### Using Maven Wrapper
```bash
./mvnw clean spring-boot:run
```

### Running Tests
```bash
./mvnw test
```

### Using Docker Compose
```bash
docker compose up -d
```
