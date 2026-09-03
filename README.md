# Product Management REST API Solution
### Java Backend Developer Hiring Assignment — Official Evaluation Submission
**Organization:** Zest India IT Pvt Ltd  
**Project:** Product Management RESTful API with Role-Based JWT & Refresh Token Rotation

---

## 1. Overview & Architecture

This project is a RESTful API solution designed to manage products and their associated items with full CRUD capabilities, pagination, role-based security, refresh token rotation, and asynchronous audit processing.

### Architecture Highlights
- **Clean Layered Architecture:**
  - `entity`: JPA Domain entities mapping the exact database structure.
  - `repository`: Spring Data JPA interfaces for data access with indexing.
  - `service` & `impl`: Business logic, transaction boundaries, and async audit logging.
  - `controller`: Versioned RESTful endpoints (`/api/v1/...`) with standard JSON envelopes.
  - `dto` (`request`/`response`): Validation-backed DTOs separating API contracts from database schema.
  - `security`: Stateless Spring Security 6 with JWT authentication & refresh token rotation.
  - `config`: Thread pool executor, OpenAPI/Swagger UI, and security filter chain.
  - `exception`: Centralized exception handler with unified error payloads.

---

## 2. Technology Stack

- **Language:** Java 17 (LTS)
- **Framework:** Spring Boot 3.2.5
- **Persistence:** Spring Data JPA (Hibernate 6)
- **Databases:**
  - **MySQL 8.0:** Production & Docker environments
  - **H2 In-Memory:** Automated Unit & Integration testing suite
- **Security:** Spring Security 6 with JJWT (JSON Web Token) & Refresh Token Rotation
- **Validation:** Jakarta Validation (`jakarta.validation-api`)
- **API Documentation:** Springdoc OpenAPI 3 / Swagger UI (`v2.5.0`)
- **Testing:** JUnit 5, Mockito, Spring Boot Test, Spring Security Test
- **Containerization:** Docker & Docker Compose (Multi-stage build)

---

## 3. Database Schema

The database structure aligns with the official technical evaluation specifications:

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

## 4. API Endpoints

All endpoints follow RESTful resource-oriented design with `/api/v1/` versioning and uniform responses.

### Authentication Endpoints (`/api/v1/auth`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Public | Register a new user (`ROLE_USER` or `ROLE_ADMIN`) |
| `POST` | `/api/v1/auth/login` | Public | Login with credentials; returns Access & Refresh tokens |
| `POST` | `/api/v1/auth/refresh-token` | Public | Rotates refresh token and returns new access token |
| `POST` | `/api/v1/auth/logout` | Authenticated | Revokes active refresh token |

### Product Endpoints (`/api/v1/products`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/products` | USER, ADMIN | Get paginated products (`page`, `size`, `sortBy`, `sortDir`, `search`) |
| `GET` | `/api/v1/products/{id}` | USER, ADMIN | Get product by ID with associated items |
| `POST` | `/api/v1/products` | ADMIN | Create new product |
| `PUT` | `/api/v1/products/{id}` | ADMIN | Update product name by ID |
| `DELETE` | `/api/v1/products/{id}` | ADMIN | Delete product by ID |
| `GET` | `/api/v1/products/{id}/items` | USER, ADMIN | Get all items for a product |
| `POST` | `/api/v1/products/{id}/items` | ADMIN | Add item to a product |
| `DELETE` | `/api/v1/products/{id}/items/{itemId}` | ADMIN | Delete item from a product |

---

## 5. Security & Performance Highlights

1. **Refresh Token Rotation:** Every time `/api/v1/auth/refresh-token` is called, the provided refresh token is invalidated and a fresh refresh token is issued along with a new access token. Stale or reused tokens are rejected with HTTP 403 Forbidden.
2. **Role-Based Access Control (RBAC):**
   - Read operations (`GET`) are permitted to both `ROLE_USER` and `ROLE_ADMIN`.
   - Mutation operations (`POST`, `PUT`, `DELETE`) require `ROLE_ADMIN`.
3. **Database Indexing:** Explicit indexes created on `product_name`, `created_on`, and `product_id` for optimal query and search performance.
4. **Asynchronous Audit Processing:** Product modifications (create, update, delete) trigger `@Async("taskExecutor")` background logging on a configured `ThreadPoolTaskExecutor`, keeping the HTTP response latency minimal.
5. **Standardized Error Handling:** Global `@RestControllerAdvice` returns structured JSON with specific validation error messages for invalid requests.

---

## 6. Default Credentials (Seeded on Startup)

For convenience during evaluation, the application automatically seeds two test accounts upon startup:
- **Admin User:**
  - Username: `admin`
  - Password: `admin123`
  - Role: `ROLE_ADMIN`
- **Regular User:**
  - Username: `user`
  - Password: `user123`
  - Role: `ROLE_USER`

---

## 7. How to Run Locally

### Prerequisites
- JDK 17+ installed
- MySQL 8.0 running locally on port 3306 (or Docker)

### Steps
1. **Configure MySQL Database:**
   Update `src/main/resources/application.properties` if needed:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/productmanagementrestapi?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
   spring.datasource.username=root
   spring.datasource.password=umesh
   ```

2. **Build and Run:**
   ```powershell
   # Run using Maven Wrapper
   .\mvnw clean spring-boot:run
   ```

3. **Access Swagger UI:**
   Open your browser at:
   [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 8. How to Run with Docker Compose

To run both MySQL 8 and the Spring Boot application in isolated containers:

```bash
# Build and launch containers
docker-compose up -d --build

# View container logs
docker-compose logs -f app

# Stop containers
docker-compose down
```

---

## 9. Running Tests

All unit tests (Mockito & JUnit 5) and integration tests (`@SpringBootTest` with H2 In-Memory database) can be executed with:

```powershell
.\mvnw clean test
```

Test Results:
- `ProductServiceTest`: Unit testing service logic and mock interactions.
- `ProductControllerTest`: Web layer testing with MockMvc and validation assertions.
- `AuthIntegrationTest`: Full authentication lifecycle and refresh token rotation with H2.
- `ProductIntegrationTest`: End-to-end CRUD operations with H2 in-memory database.

---

## 10. Sample cURL Requests

### Login as Admin:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### Create Product (as ADMIN):
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"productName": "Sony WH-1000XM5 Headphones"}'
```

### Get All Products with Pagination:
```bash
curl -X GET "http://localhost:8080/api/v1/products?page=0&size=10&sortBy=productName&sortDir=asc" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

### Add Item to Product:
```bash
curl -X POST http://localhost:8080/api/v1/products/1/items \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 40}'
```

### Refresh Token Rotation:
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "<REFRESH_TOKEN>"}'
```
