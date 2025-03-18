-- Create the database
CREATE DATABASE duty_tracker;
USE duty_tracker;

-- Admin Table
CREATE TABLE admin (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- Hashed password
    role ENUM('Super Admin', 'Department Admin') DEFAULT 'Department Admin',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    session_token VARCHAR(255) NULL;
);

-- Student Table
CREATE TABLE students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) UNIQUE NOT NULL,  -- Unique Student ID
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- Hashed password
    scholarship_type VARCHAR(100),
    course VARCHAR(100) NOT NULL,  -- Student course
    department VARCHAR(100) NOT NULL,  -- Student department
    hk_duty_status VARCHAR(100) NOT NULL,  -- Custom duty status set from frontend
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    profile_picture VARCHAR(255) DEFAULT NULL;
);


-- Duty Logs Table
CREATE TABLE duty_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    time_in DATETIME NOT NULL,
    time_out DATETIME,
    total_hours DECIMAL(5,2),  -- Stores total hours for a session
    status ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending',
    admin_id INT,
    approved_at TIMESTAMP NULL,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES admin(id) ON DELETE SET NULL
);

-- Notifications Table
CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,  -- Admin or Student ID
    role ENUM('Admin', 'Student') NOT NULL,  
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Email Verification Table
CREATE TABLE email_verifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,  -- Can be either student or admin ID
    role ENUM('Admin', 'Student') NOT NULL,
    token VARCHAR(255) NOT NULL,
    expires_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES students(id) ON DELETE CASCADE
);

-- Insert a default Super Admin
INSERT INTO admin (name, email, password, role) 
VALUES ('Super Admin', 'admin@example.com', 'HASHED_PASSWORD_HERE', 'Super Admin');
