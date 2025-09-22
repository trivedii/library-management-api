-- 1. Users table (who borrow books & manage wishlists)
CREATE TABLE users (
    Id BIGINT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(255) NOT NULL,
    Email VARCHAR(255) UNIQUE NOT NULL,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CreatedBy VARCHAR(255),
    UpdatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UpdatedBy VARCHAR(255)
) ENGINE=InnoDB;

-- 2. Books table (inventory of library)
CREATE TABLE books (
    Id BIGINT AUTO_INCREMENT PRIMARY KEY,
    Title VARCHAR(255) NOT NULL,
    Author VARCHAR(255) NOT NULL,
    Isbn VARCHAR(20) UNIQUE NOT NULL,
    PublishedYear YEAR NOT NULL,
    AvailabilityStatus ENUM('Available', 'Borrowed') DEFAULT 'Available',
    IsDeleted BOOLEAN NOT NULL DEFAULT FALSE,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CreatedBy VARCHAR(255),
    UpdatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UpdatedBy VARCHAR(255)
) ENGINE=InnoDB;

-- 3. Wishlist table (many-to-many: users wishlisting books)
CREATE TABLE wishlists (
    Id BIGINT AUTO_INCREMENT PRIMARY KEY,
    UserId BIGINT NOT NULL,
    BookId BIGINT NOT NULL,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (UserId) REFERENCES users(Id),
    FOREIGN KEY (BookId) REFERENCES books(Id),
    UNIQUE (UserId, BookId) -- one user can't wishlist same book twice
) ENGINE=InnoDB;

-- 5. Borrowed Books table (tracks book lending history)
CREATE TABLE borrowed_books (
    Id BIGINT AUTO_INCREMENT PRIMARY KEY,
    UserId BIGINT NOT NULL,
    BookId BIGINT NOT NULL,
    BorrowedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ReturnedAt TIMESTAMP NULL,
    FOREIGN KEY (UserId) REFERENCES users(Id),
    FOREIGN KEY (BookId) REFERENCES books(Id)
) ENGINE=InnoDB;

ALTER TABLE books
ADD FULLTEXT INDEX idx_books_title_author (Title, Author);


