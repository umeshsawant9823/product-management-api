# Product Management REST API

### Java Backend Developer Hiring Assignment — Official Evaluation Submission
**Organization:** Zest India IT Pvt Ltd  
**Project:** Product Management RESTful API with Role-Based JWT & Refresh Token Rotation

---

## Overview & Architecture

Product Management REST API is a production-grade RESTful web service built with Java 17 and Spring Boot. It provides full CRUD operations for products and their associated items, complete with pagination, sorting, search capabilities, role-based access control, secure JWT authentication with refresh token rotation, and asynchronous audit processing.

### Architectural Layers
- **Controller Layer (`org.techhub.controller`):** Exposes versioned RESTful endpoints (`/api/v1/...`) with standardized JSON response envelopes.
- **Service Layer (`org.techhub.service`):** Encapsulates business logic, transactional integrity, and asynchronous audit events.
- **Data Access Layer (`org.techhub.repository`):** Spring Data JPA repositories with custom query and indexing support.
- **Domain Entities (`org.techhub.entity`):** JPA entities mapping the database schema with explicit indexes and relationship cascades.
- **DTO Layer (`org.techhub.dto`):** Strongly-typed Request and Response models with Jakarta Validation annotations to isolate API contracts from persistence models.
- **Security & JWT (`org.techhub.security` & `org.techhub.config`):** Stateless security filter chain enforcing Bearer token authentication, role validation, and CORS policies.
- **Exception Handling (`org.techhub.exception`):** Centralized `@RestControllerAdvice` delivering structured error responses for validation failures, resource misses, and unauthorized operations.

---

## Technology Stack

- **Language:** Java 17 (LTS)
- **Framework:** Spring Boot 3.2.5
- **Persistence:** Spring Data JPA / Hibernate 6
- **Relational Databases:**
  - **MySQL 8.0:** Default database for local runtime and Docker deployment
  - **H2 In-Memory Database:** Isolated test execution (`MODE=MySQL`)
- **Security:** Spring Security 6, JJWT (`io.jsonwebtoken` 0.11.5), BCrypt password hashing
- **Validation:** Jakarta Validation (`jakarta.validation-api`)
- **API Documentation:** Springdoc OpenAPI 3 / Swagger UI (`v2.5.0`)
- **Testing:** JUnit 5, Mockito, Spring Boot Test, Spring Security Test
- **Containerization:** Docker (Multi-stage build) & Docker Compose

---

## Database Schema

The entity models and database structure directly adhere to the official evaluation specification:

### `product` Table
```sql
CREATE TABLE product (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(255) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP NOT NULL,
    modified_by VARCHAR(100),
    modified_on TIMESTAMP
);
-- Indexes: idx_product_name, idx_created_on
```

### `item` Table
```sql
CREATE TABLE item (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);
-- Index: idx_item_product_id
```

### `users` & `refresh_token` Tables
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE refresh_token (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

---

## API Endpoints

All endpoints follow resource-oriented REST conventions with `/api/v1/` versioning and uniform JSON envelopes (`ApiResponse<T>`).

### Authentication Endpoints (`/api/v1/auth`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Public | Register a new user (`ROLE_USER` or `ROLE_ADMIN`) |
| `POST` | `/api/v1/auth/login` | Public | Authenticate user; returns access token and refresh token |
| `POST` | `/api/v1/auth/refresh-token` | Public | Validate refresh token, issue rotated refresh token and new access token |
| `POST` | `/api/v1/auth/logout` | Authenticated | Revoke active refresh token for the authenticated user |

### Product Endpoints (`/api/v1/products`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/products` | USER, ADMIN | Get paginated products (`page`, `size`, `sortBy`, `sortDir`, `search`) |
| `GET` | `/api/v1/products/{id}` | USER, ADMIN | Get product details by ID including associated items |
| `POST` | `/api/v1/products` | ADMIN | Create a new product (records `createdBy` and `createdOn`) |
| `PUT` | `/api/v1/products/{id}` | ADMIN | Update product name by ID (records `modifiedBy` and `modifiedOn`) |
| `DELETE` | `/api/v1/products/{id}` | ADMIN | Delete product and its associated items by ID |
| `GET` | `/api/v1/products/{id}/items` | USER, ADMIN | Retrieve all items associated with a product |
| `POST` | `/api/v1/products/{id}/items` | ADMIN | Add an item with quantity to the specified product |
| `DELETE` | `/api/v1/products/{id}/items/{itemId}` | ADMIN | Delete an item from the specified product |

---

## Security & Performance

1. **Refresh Token Rotation:** Each invocation of `/api/v1/auth/refresh-token` validates the submitted refresh token, revokes/deletes it from the database, and issues an entirely new refresh token alongside a fresh access token. Replayed or expired tokens are rejected with HTTP 403 Forbidden.
2. **Role-Based Authorization (RBAC):**
   - Read operations (`GET`) are permitted to authenticated users with either `ROLE_USER` or `ROLE_ADMIN`.
   - Mutation operations (`POST`, `PUT`, `DELETE`) are restricted to `ROLE_ADMIN`.
3. **Database Indexing Strategy:** Explicit database indexes are applied on `product_name`, `created_on`, and `product_id` to ensure optimal lookup and sorting performance.
4. **Asynchronous Audit Processing:** State modifications (product creation, updates, deletions) trigger background audit logging via `@Async("taskExecutor")` using a dedicated `ThreadPoolTaskExecutor`, keeping endpoint latency minimal.
5. **Standardized Error Handling:** Centralized `@RestControllerAdvice` captures validation errors (`MethodArgumentNotValidException`), missing entities (`ResourceNotFoundException`), bad requests (`BadRequestException`), and security errors into a consistent JSON response.
6. **CORS & Stateless Session:** Configured with `SessionCreationPolicy.STATELESS`, explicit CORS origins and headers, and disabled CSRF for stateless REST interaction.

---

## Environment Configuration

Sensitive credentials and environment-specific parameters should be supplied via environment variables or external configuration:

| Variable | Description | Default / Example Value |
| :--- | :--- | :--- |
| `DB_USERNAME` | MySQL database username | `root` |
| `DB_PASSWORD` | MySQL database user password | `${DB_PASSWORD}` |
| `JWT_SECRET` | 256-bit key used for signing HMAC-SHA256 JWT tokens | `${JWT_SECRET}` |

In `src/main/resources/application.properties`, configure database connectivity using environment properties:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/productmanagementrestapi?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}
```

---

## Default Evaluation Credentials

For seamless evaluation, the application includes a `DataInitializer` that automatically seeds test accounts on startup if the database is empty:

> [!NOTE]
> The credentials below are intended strictly for local development and evaluation purposes.

| Role | Username | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `admin123` | Full access: CRUD on products, items, and view operations |
| **Standard User** | `user` | `user123` | Read-only access: View products and items |

---

## Local Setup

### Prerequisites
- JDK 17+ installed
- MySQL 8.0 running on `localhost:3306`

### Running the Application
1. Clone the repository and navigate to the project root:
   ```bash
   git clone https://github.com/umeshsawant9823/product-management-api.git
   cd product-management-api
   ```

2. Verify or update MySQL credentials in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.username=${DB_USERNAME:root}
   spring.datasource.password=${DB_PASSWORD}
   ```

3. Build and launch using the Maven Wrapper:
   ```powershell
   .\mvnw clean spring-boot:run
   ```

4. The service will be available at `http://localhost:8080`.

---

## Docker Compose

To deploy the application alongside a containerized MySQL 8 database with automatic health checks:

```bash
# Build and run services in detached mode
docker-compose up -d --build

# Inspect application logs
docker-compose logs -f app

# Tear down containers and networks
docker-compose down
```

---

## Testing

The project includes an automated test suite combining unit tests and integration tests with an in-memory H2 database.

Run the test suite via Maven:
```powershell
.\mvnw clean test
```

### Test Coverage Summary:
- **`ProductServiceTest` (Unit Test):** Tests business logic, pagination, validation, and async audit invocation using JUnit 5 and Mockito.
- **`ProductControllerTest` (Web Layer Test):** Validates HTTP status codes, JSON serialization, input validation, and role-based access control via MockMvc.
- **`AuthIntegrationTest` (Integration Test):** Tests end-to-end user registration, credential login, and refresh token rotation with H2.
- **`ProductIntegrationTest` (Integration Test):** Executes the full product lifecycle (creation, item association, update, search, deletion) against an in-memory H2 database.

---

## Swagger

Interactive API documentation with integrated JWT Bearer authentication is provided via Springdoc OpenAPI 3.

Access Swagger UI in your browser:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### How to Authenticate in Swagger UI:
1. Call `POST /api/v1/auth/login` with your credentials (`admin` / `admin123`).
2. Copy the `accessToken` from the JSON response.
3. Click the **Authorize** button (lock icon) at the top right of the Swagger UI page.
4. Paste the token into the value field and click **Authorize**.

---

## Sample cURL Requests

### 1. User Login (Obtain Tokens)
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### 2. Create Product (Requires ADMIN Role)
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "Dell UltraSharp 27 Monitor"
  }'
```

### 3. Query Products with Pagination and Search
```bash
curl -X GET "http://localhost:8080/api/v1/products?page=0&size=10&sortBy=productName&sortDir=asc&search=Dell" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

### 4. Add Item to Product
```bash
curl -X POST http://localhost:8080/api/v1/products/1/items \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 25
  }'
```

### 5. Rotate Refresh Token
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<REFRESH_TOKEN>"
  }'
```

### 6. User Logout
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```
