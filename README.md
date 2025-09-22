# Library Management API

A Spring Boot REST API for managing library book inventory with advanced search capabilities and asynchronous wishlist notifications. Features full-text search, soft delete, batch operations, and event-driven notifications using Redis Streams.

## 📡 API Endpoints

### Base URL: `/api/library`

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| `POST` | `/books` | Create a new book | `BookDTO` | `ResponseDTO` |
| `PATCH` | `/books` | Update existing book | `BookDTO` | `ResponseDTO` |
| `GET` | `/books/search` | Search books with filters | Query Params | `BookSearchResponseDTO` |
| `DELETE` | `/books/{bookId}` | Delete single book | - | `ResponseDTO` |
| `DELETE` | `/books/delete-batch` | Delete multiple books | `Set<Long>` | `DeleteBooksResponseDTO` |

### Search Parameters
- `searchText` - Text to search in title/author (min 3 chars)
- `publishedYear` - Filter by publication year  
- `limit` - Results per page (1-100, default: 25)
- `offset` - Records to skip (default: 0)

## 🛠 Technology Stack

- **Java 17** + **Spring Boot 3.2.0**
- **MySQL 8.0** with **jOOQ 3.18.7** for type-safe SQL
- **Redis** for async processing with Streams
- **Flyway** for database migrations
- **Maven** for dependency management

## ⚙️ Setup Instructions

### Prerequisites
- Java 17+, Maven 3.6+, MySQL 8.0+, Redis 6.0+

### 1. Clone & Setup Database
```bash
git clone <repository-url>
cd library-management-api

# Create MySQL database
mysql -u root -p -e "CREATE DATABASE library_management;"
```

### 2. Environment Variables
```bash
cp env.example .env
# Edit .env with your database and Redis settings
```

**Required Environment Variables:**
```properties
DB_URL=jdbc:mysql://localhost:3306/library_management?createDatabaseIfNotExist=true&useSSL=false
DB_USERNAME=root
DB_PASSWORD=your_password
DB_SCHEMA=library_management
REDIS_HOST=localhost
REDIS_PORT=6379
```

### 3. Run Application
```bash
mvn clean install
mvn spring-boot:run
```

Application starts on `http://localhost:8080`

## 📊 Database Schema

**From Flyway Migration: `V1__Create_initial_schema.sql`**

```sql
-- Books table (main inventory)
CREATE TABLE books (
    Id BIGINT AUTO_INCREMENT PRIMARY KEY,
    Title VARCHAR(255) NOT NULL,
    Author VARCHAR(255) NOT NULL,
    Isbn VARCHAR(20) UNIQUE NOT NULL,
    PublishedYear YEAR NOT NULL,
    AvailabilityStatus ENUM('Available', 'Borrowed') DEFAULT 'Available',
    IsDeleted BOOLEAN NOT NULL DEFAULT FALSE,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UpdatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Users table
CREATE TABLE users (
    Id BIGINT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(255) NOT NULL,
    Email VARCHAR(255) UNIQUE NOT NULL,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Wishlist table
CREATE TABLE wishlists (
    Id BIGINT AUTO_INCREMENT PRIMARY KEY,
    UserId BIGINT NOT NULL,
    BookId BIGINT NOT NULL,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (UserId) REFERENCES users(Id),
    FOREIGN KEY (BookId) REFERENCES books(Id),
    UNIQUE (UserId, BookId)
) ENGINE=InnoDB;

-- Borrowed books tracking
CREATE TABLE borrowed_books (
    Id BIGINT AUTO_INCREMENT PRIMARY KEY,
    UserId BIGINT NOT NULL,
    BookId BIGINT NOT NULL,
    BorrowedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ReturnedAt TIMESTAMP NULL,
    FOREIGN KEY (UserId) REFERENCES users(Id),
    FOREIGN KEY (BookId) REFERENCES books(Id)
) ENGINE=InnoDB;

-- Performance index for search
ALTER TABLE books ADD FULLTEXT INDEX idx_books_title_author (Title, Author);
```

---

**Built with ❤️ for efficient library management**