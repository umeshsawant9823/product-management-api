# Product Management REST API

A Spring Boot REST API for managing products and their items. Built with Java 17, Spring Boot, MySQL, and Spring Security with JWT.

---

## Features

- **Product CRUD**: Endpoints to create, view, update, and delete products.
- **Product Items**: Endpoints to add items to a product, view items by product ID, and delete items.
- **Pagination and Sorting**: Product list supports pagination (`page`, `size`), sorting (`sortBy`, `sortDir`), and name search.
- **Authentication with JWT**:
  - User registration and login.
  - Generates JWT access token and refresh token.
  - Refresh token rotation (when refreshing, old refresh token is replaced by a new one).
  - Role-based security: `ADMIN` can create, update, and delete; `USER` can only view.
- **Validation**: Request data validation using Jakarta validation annotations (`@NotBlank`, `@Min`, `@Size`).
- **Async Logging**: Asynchronous audit logging when products are created, updated, or deleted using `@Async`.
- **API Documentation**: Swagger UI for viewing and testing endpoints.
- **Unit and Integration Tests**: 14 automated tests using JUnit 5, Mockito, and in-memory H2 database.
- **Docker Support**: Dockerfile and docker-compose file included.

---

## Tech Stack

- Java 17
- Spring Boot 3.2.5
- Spring Data JPA (Hibernate)
- MySQL 8 (H2 database used for running tests)
- Spring Security & JJWT
- Maven

---

## Project Structure

```
src/main/java/org/techhub/
├── config/          # SecurityConfig, OpenApiConfig, AsyncConfig, DataInitializer
├── controller/      # AuthController, ProductController, ItemController
├── dto/             # Request and Response DTOs
├── entity/          # Product, Item, User, RefreshToken, Role
├── exception/       # ResourceNotFoundException, GlobalExceptionHandler
├── repository/      # Spring Data JPA repositories
├── security/        # JwtUtils, JwtAuthenticationFilter, UserDetailsServiceImpl
└── service/         # Service interfaces and implementations
```

---

## Database Design

### product
- `id` (Primary Key, Auto-increment)
- `product_name` (VARCHAR 255, NOT NULL)
- `created_by` (VARCHAR 100, NOT NULL)
- `created_on` (TIMESTAMP, NOT NULL)
- `modified_by` (VARCHAR 100)
- `modified_on` (TIMESTAMP)

### item
- `id` (Primary Key, Auto-increment)
- `product_id` (Foreign Key referencing product.id)
- `quantity` (INT, NOT NULL)

### users and refresh_token
- `users`: Stores username, email, hashed password, and role (`ROLE_USER`, `ROLE_ADMIN`).
- `refresh_token`: Stores refresh tokens linked to users for token rotation.

---

## Default Credentials (For Testing)

Two accounts are automatically created on application startup if the database is empty:

| Role | Username | Password | Permissions |
| :--- | :--- | :--- | :--- |
| Admin | `admin` | `admin123` | Can view, create, update, and delete products/items |
| User | `user` | `user123` | Can only view products and items |

---

## API Endpoints

All endpoints start with `/api/v1`.

### Authentication Endpoints (`/api/v1/auth`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/v1/auth/register` | Public | Register a new user |
| POST | `/api/v1/auth/login` | Public | Login and get access token + refresh token |
| POST | `/api/v1/auth/refresh-token` | Public | Refresh access token and rotate refresh token |
| POST | `/api/v1/auth/logout` | Authenticated | Logout and revoke refresh token |

### Product Endpoints (`/api/v1/products`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/products` | USER, ADMIN | Get products with pagination, sorting, and search |
| GET | `/api/v1/products/{id}` | USER, ADMIN | Get single product with its items |
| POST | `/api/v1/products` | ADMIN | Create new product |
| PUT | `/api/v1/products/{id}` | ADMIN | Update product name |
| DELETE | `/api/v1/products/{id}` | ADMIN | Delete product |
| GET | `/api/v1/products/{id}/items` | USER, ADMIN | Get items for a product |
| POST | `/api/v1/products/{id}/items` | ADMIN | Add item to a product |
| DELETE | `/api/v1/products/{id}/items/{itemId}` | ADMIN | Delete item from a product |

---

## How to Run Locally

### 1. Prerequisites
- Java 17
- MySQL running on port 3306

### 2. Configure Database
In `src/main/resources/application.properties`, check or update your MySQL credentials:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/productmanagementrestapi?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### 3. Run the Application
Open a terminal in the project folder and run:

```powershell
.\mvnw clean spring-boot:run
```

The application will start at: `http://localhost:8080`

---

## How to Run with Docker Compose

If you have Docker installed, you can run the app and MySQL together:

```bash
# Start MySQL and Spring Boot app
docker-compose up --build

# Stop containers
docker-compose down
```

---

## Running Tests

The project includes unit tests and integration tests with an in-memory H2 database.

To run all tests:
```powershell
.\mvnw test
```

- `ProductServiceTest`: Unit tests using Mockito.
- `ProductControllerTest`: Web layer tests using MockMvc.
- `AuthIntegrationTest`: Tests register, login, and refresh token rotation with H2.
- `ProductIntegrationTest`: Tests full product and item CRUD flow with H2.

---

## Swagger UI

You can test the APIs in your browser:

Link: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### How to use Swagger with JWT:
1. Run `POST /api/v1/auth/login` using `admin` / `admin123`.
2. Copy the `accessToken` from the response.
3. Click the **Authorize** button at the top right of the Swagger page.
4. Enter the token and click **Authorize**.
5. You can now execute the protected endpoints directly.

---

## Sample cURL Requests

### 1. Login as Admin
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### 2. Create Product (Requires Admin Token)
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"productName": "Wireless Keyboard"}'
```

### 3. Get Products (with Pagination)
```bash
curl -X GET "http://localhost:8080/api/v1/products?page=0&size=10&sortBy=productName&sortDir=asc" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

### 4. Add Item to Product
```bash
curl -X POST http://localhost:8080/api/v1/products/1/items \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 20}'
```

### 5. Refresh Token
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "<REFRESH_TOKEN>"}'
```
