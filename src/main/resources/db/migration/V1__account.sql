CREATE TABLE account
(
    id VARCHAR(40) NOT NULL PRIMARY KEY,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    email VARCHAR(255)
)