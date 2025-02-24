CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    duty_status ENUM('Office Duty', 'Student Facilitator', 'HK BBS') DEFAULT 'Select Duty Status',
    scholarship_type ENUM('25%', '50%', '75%') DEFAULT 'Select Scholar Type',
    course ENUM('CITE', 'CMA', 'CRIM') DEFAULT 'Select Course',
    year_level ENUM('First Year', 'Second Year', 'Third Year', 'Fourth Year') DEFAULT 'Select Year'
);