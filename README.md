# E-Commerce REST API

A production-ready e-commerce backend built with **Spring Boot 3**, **Spring Security + JWT**, **Spring Data JPA**, and **MySQL**.

**Live API:** `https://e-commerce-project-backend-f84h.onrender.com`

> Note: First request may take 30-60 seconds to wake up (free tier cold start).

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security 6 + JWT (stateless) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8 |
| Validation | Jakarta Bean Validation |
| Build | Maven |

---

## Project Structure

```
src/main/java/com/ecommerce/api/
├── config/          # Security configuration
├── controller/      # REST controllers
├── dto/
│   ├── request/     # Input DTOs (validated)
│   └── response/    # Output DTOs (no entity exposure)
├── exception/       # Custom exceptions + global handler
├── model/           # JPA entities
├── repository/      # Spring Data JPA repositories
├── security/        # JWT utils + filter + UserDetailsService
└── service/         # Business logic
```

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8

### 1. Configure the database

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
```

### 2. Run

```bash
mvn spring-boot:run
```

App starts at `http://localhost:8080`.

---

## API Reference

### Authentication (public)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create a new user account |
| POST | `/api/auth/login` | Login and receive a JWT token |

**Login response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "username": "john",
  "role": "ROLE_USER"
}
```

All protected endpoints require:
```
Authorization: Bearer <token>
```

---

### Products (public read)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/products` | Paginated list (`?page=0&size=10&sort=name`) |
| GET | `/api/products/{id}` | Single product |
| GET | `/api/products/search?name=apple` | Search by name |
| GET | `/api/products/category/{id}` | Filter by category |

### Categories (public read)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/categories` | All categories |
| GET | `/api/categories/{id}` | Single category |

---

### User (authenticated)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users/me` | View own profile |
| PUT | `/api/users/me` | Update profile |

### Cart (authenticated)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/cart` | View cart with totals |
| POST | `/api/cart/items` | Add item `{ "productId": 1, "quantity": 2 }` |
| PUT | `/api/cart/items/{itemId}` | Update item quantity |
| DELETE | `/api/cart/items/{itemId}` | Remove item |
| DELETE | `/api/cart` | Clear cart |

---

### Admin (ROLE_ADMIN only)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/customers` | List all users |
| GET | `/api/admin/customers/{id}` | Single user |
| DELETE | `/api/admin/customers/{id}` | Delete user |
| POST | `/api/admin/categories` | Create category |
| PUT | `/api/admin/categories/{id}` | Update category |
| DELETE | `/api/admin/categories/{id}` | Delete category |
| POST | `/api/admin/products` | Create product |
| PUT | `/api/admin/products/{id}` | Update product |
| DELETE | `/api/admin/products/{id}` | Delete product |

---

## Error Responses

All errors return a consistent JSON shape:

```json
{
  "status": 404,
  "message": "Product not found with id: 5",
  "timestamp": "2026-01-01T10:00:00Z"
}
```

Validation failures return field-level errors:

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "email": "Must be a valid email address",
    "password": "Password must be at least 6 characters"
  }
}
```

---

## Running Tests

```bash
mvn test
```
