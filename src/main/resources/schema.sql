CREATE TABLE IF NOT EXISTS players (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    games_played INT DEFAULT 0,
    games_won INT DEFAULT 0,
    games_lost INT DEFAULT 0
);
