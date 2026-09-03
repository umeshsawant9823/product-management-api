# Product Management REST API

A REST API built with **Java 17** and **Spring Boot** to manage products and their items. This project was developed as part of the Java Backend Developer hiring assignment for **Zest India IT Pvt Ltd**.

---

## Features

- **Product CRUD:** Create, read, update, and delete products.
- **Product Items:** Add, view, and delete items with quantities linked to a product.
- **Pagination & Search:** Get products with pagination, sorting (`sortBy`, `sortDir`), and keyword search.
- **Authentication & Security:** 
  - JWT-based authentication (Access Token + Refresh Token).
  - Refresh Token rotation (old token is revoked, new token is generated).
  - Role-based authorization (`ROLE_ADMIN` can create/update/delete, `ROLE_USER` can read).
- **Input Validation:** Validation using Jakarta annotations (`@NotBlank`, `@Min`, `@Size`, etc.).
- **Async Processing:** Asynchronous audit logging when products are created, updated, or deleted.
- **API Documentation:** Interactive Swagger UI documentation.
- **Automated Tests:** Unit tests (Mockito) and integration tests with an in-memory H2 database.
- **Docker Support:** Containerized setup using `Dockerfile` and `docker-compose.yml`.

---

## Tech Stack

- **Java:** 17
- **Framework:** Spring Boot 3.2.5
- **Database:** MySQL 8 (H2 in-memory for testing)
- **ORM:** Spring Data JPA / Hibernate
- **Security:** Spring Security 6 & JJWT (0.11.5)
- **Documentation:** Springdoc OpenAPI / Swagger UI
- **Testing:** JUnit 5, Mockito, Spring Boot Test
- **Build Tool:** Maven

---

## Project Structure

```
src/main/java/org/techhub/
├── config/          # Security, Swagger, Async, and DataInitializer
├── controller/      # Auth, Product, and Item REST controllers
├── dto/             # Request and Response models
├── entity/          # Product, Item, User, RefreshToken, Role
├── exception/       # Custom exceptions and GlobalExceptionHandler
├── repository/      # Spring Data JPA repositories
├── security/        # JWT utility, filter, and UserDetailsService
└── service/         # Service interfaces and implementations
```

---

## Database Tables

The database schema matches the assignment requirements:

### 1. `product`
- `id` (Primary Key, Auto-increment)
- `product_name` (VARCHAR 255, Not Null)
- `created_by` (VARCHAR 100, Not Null)
- `created_on` (TIMESTAMP, Not Null)
- `modified_by` (VARCHAR 100)
- `modified_on` (TIMESTAMP)

### 2. `item`
- `id` (Primary Key, Auto-increment)
- `product_id` (Foreign Key referencing `product.id`)
- `quantity` (INT, Not Null)

### 3. `users` & `refresh_token`
- `users`: Stores user credentials (`username`, `email`, `password`, `role`).
- `refresh_token`: Tracks refresh tokens, expiration time, and revocation status for token rotation.

---

## Default Login Credentials (For Testing)

The application automatically seeds two test users on startup if the database is empty:

| Role | Username | Password | Access Level |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Can view, create, edit, and delete products & items |
| **User** | `user` | `user123` | Can view products and items (read-only) |

---

## API Endpoints

All APIs use the `/api/v1/` prefix.

### Authentication (`/api/v1/auth`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Public | Register a new user |
| `POST` | `/api/v1/auth/login` | Public | Login to get Access Token and Refresh Token |
| `POST` | `/api/v1/auth/refresh-token` | Public | Rotate refresh token and get a new access token |
| `POST` | `/api/v1/auth/logout` | Authenticated | Revoke current refresh token |

### Products & Items (`/api/v1/products`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/products` | USER, ADMIN | Get products with pagination & search |
| `GET` | `/api/v1/products/{id}` | USER, ADMIN | Get single product by ID |
| `POST` | `/api/v1/products` | ADMIN | Create a new product |
| `PUT` | `/api/v1/products/{id}` | ADMIN | Update an existing product |
| `DELETE` | `/api/v1/products/{id}` | ADMIN | Delete a product |
| `GET` | `/api/v1/products/{id}/items` | USER, ADMIN | Get items for a product |
| `POST` | `/api/v1/products/{id}/items` | ADMIN | Add item to a product |
| `DELETE` | `/api/v1/products/{id}/items/{itemId}` | ADMIN | Delete an item from a product |

---

## How to Run Locally

### Prerequisites
- Java 17 or higher
- MySQL running on port 3306

### Steps
1. **Clone the repository:**
   ```bash
   git clone https://github.com/umeshsawant9823/product-management-api.git
   cd product-management-api
   ```

2. **Configure Database:**
   Update your database credentials in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/productmanagementrestapi?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
   spring.datasource.username=${DB_USERNAME:root}
   spring.datasource.password=${DB_PASSWORD}
   ```

3. **Run the application:**
   ```powershell
   .\mvnw spring-boot:run
   ```

4. The server will start at `http://localhost:8080`.

---

## How to Run with Docker

You can run both MySQL and the Spring Boot application together using Docker Compose:

```bash
# Build and start containers
docker-compose up --build

# Stop containers
docker-compose down
```

---

## Testing

The project has 14 automated tests covering services, controllers, and integration flows using JUnit 5, Mockito, and an in-memory H2 database:

```powershell
.\mvnw test
```

- **Unit tests (`ProductServiceTest`):** Tests business logic with Mockito mocks.
- **Controller tests (`ProductControllerTest`):** Tests HTTP status codes, validation, and role checks.
- **Integration tests (`AuthIntegrationTest`, `ProductIntegrationTest`):** Tests complete authentication, token rotation, and product lifecycle with H2.

---

## Swagger Documentation

You can test all endpoints in your browser using Swagger UI:

👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

### How to use Swagger with JWT:
1. Call `POST /api/v1/auth/login` with `admin` / `admin123`.
2. Copy the `accessToken` from the response.
3. Click the green **Authorize** button at the top right.
4. Enter the token and click **Authorize**.
5. You can now test protected endpoints directly from Swagger.

---

## Sample cURL Requests

### 1. Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### 2. Create Product (ADMIN only)
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer <YOUR_ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"productName": "Wireless Mouse"}'
```

### 3. Get Products (Paginated)
```bash
curl -X GET "http://localhost:8080/api/v1/products?page=0&size=5&sortBy=productName&sortDir=asc" \
  -H "Authorization: Bearer <YOUR_ACCESS_TOKEN>"
```

### 4. Add Item to Product
```bash
curl -X POST http://localhost:8080/api/v1/products/1/items \
  -H "Authorization: Bearer <YOUR_ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 10}'
```

### 5. Refresh Token Rotation
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "<YOUR_REFRESH_TOKEN>"}'
```
