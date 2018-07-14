CREATE TABLE session
(
    id VARCHAR(40) NOT NULL PRIMARY KEY,
    ownerId VARCHAR(40),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expiresAt TIMESTAMP NULL
)