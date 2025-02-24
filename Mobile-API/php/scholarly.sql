CREATE DATABASE IF NOT EXISTS scholarly;
USE scholarly;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data
INSERT INTO users (student_id, email, password) VALUES
('2024001', 'student1@mail.com', '$2y$10$abcdefghijklmnopqrstuv'),
('2024002', 'student2@mail.com', '$2y$10$abcdefghijklmnopqrstuv');
