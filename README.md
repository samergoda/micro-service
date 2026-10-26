# Micro-Service Demo

## Overview

This repository contains a simple **micro‑service architecture** built with **Spring Boot** and **Maven**. It demonstrates how to structure multiple independent services that communicate via HTTP/REST and are orchestrated with Docker Compose.

The project consists of four services:

- **gateway** – API gateway (Spring Cloud Gateway) that routes requests to the downstream services.
- **notifications** – Manages notification creation and persistence.
- **orders** – Handles order creation, updates, and querying.
- **users** – Provides CRUD operations for user management.

Each service lives in its own module with its own `pom.xml`, Dockerfile, and `src/` directory.

## Architecture Diagram

```mermaid
graph LR
    GW[Gateway] --> O[Orders Service]
    GW --> N[Notifications Service]
    GW --> U[Users Service]
    O --> DB_O[MySQL (Orders DB)]
    N --> DB_N[MySQL (Notifications DB)]
    U --> DB_U[MySQL (Users DB)]
    GW --> K[Kafka]
    O --> K
    N --> K
    U --> K
```

## Prerequisites

- **Java 27** (or compatible JDK)
- **Maven 3.9+**
- **Docker** & **Docker Compose** (for containerised run)
- **Git** (to clone the repo)

## Building the Services

From the project root, you can build all modules with:

```bash
mvn clean package -DskipTests
```

Or build a specific service, e.g. the *orders* service:

```bash
cd orders
mvn clean package -DskipTests
```

The generated JAR files will be located under `target/` of each module.

## Running Locally (without Docker)

1. Start each service in separate terminals:
   ```bash
   cd gateway && ./mvnw spring-boot:run
   cd notifications && ./mvnw spring-boot:run
   cd orders && ./mvnw spring-boot:run
   cd users && ./mvnw spring-boot:run
   ```
2. The gateway listens on **http://localhost:8080** and forwards to the services running on their default ports (8081‑8084).

## Running with Docker Compose

A `docker-compose.yml` (and individual `compose.yaml` files) are provided for each service. To spin up the entire stack:

```bash
docker compose up --build
```

The services will be reachable via the gateway at `http://localhost:8080`.

### Stopping the stack

```bash
docker compose down
```

## Testing

Unit and integration tests are included in each module. Run all tests with:

```bash
mvn test
```

Or run tests for a specific service:

```bash
cd users && mvn test
```

## API Documentation

Each Spring Boot service exposes Swagger UI when the `springdoc-openapi-ui` dependency is active. Access the docs at:

- Gateway: `http://localhost:8080/swagger-ui.html`
- Users: `http://localhost:8081/swagger-ui.html`
- Orders: `http://localhost:8082/swagger-ui.html`
- Notifications: `http://localhost:8083/swagger-ui.html`

## Contributing

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/awesome-feature`).
3. Commit your changes and push to your fork.
4. Open a Pull Request targeting the `main` branch.

Please ensure code follows the existing style and all tests pass before submitting.

## License

This project is licensed under the **MIT License** – see the `LICENSE` file for details.
