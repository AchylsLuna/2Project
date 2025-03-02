CREATE DATABASE scholarly;
USE scholarly;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

INSERT INTO users (username, password) VALUES ('jonas', 'jonas123');



CREATE TABLE user_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    date DATE NOT NULL,
    time_in TIME NOT NULL,
    time_out TIME NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

