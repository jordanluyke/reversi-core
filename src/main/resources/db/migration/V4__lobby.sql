CREATE TABLE lobby
(
    id VARCHAR(40) NOT NULL PRIMARY KEY,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    startingAt TIMESTAMP NULL,
    name VARCHAR(40) NULL,
    playerIdDark VARCHAR(40),
    playerIdLight VARCHAR(40) NULL,
    closedAt TIMESTAMP NULL,
    isPrivate BIT DEFAULT 0,
    playerReadyDark BIT DEFAULT 0,
    playerReadyLight BIT DEFAULT 0,
    matchId VARCHAR(40) NULL
)