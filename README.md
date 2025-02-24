# Mobile-API README.md

# Mobile API Project

This project is a mobile API designed to handle user registration and manage user details for students. It includes functionalities for registering users, validating input, and storing user information in a database.

## Project Structure

```
Mobile-API
├── php
│   ├── register.php          # Handles user registration
│   ├── connection
│   │   └── config.php       # Database connection settings
├── type.php                  # Defines types and constants related to the application
├── database
│   └── schema.sql           # SQL schema for the database
└── README.md                 # Project documentation
```

## Setup Instructions

1. **Clone the repository** to your local machine.
2. **Set up the database**:
   - Create a new database in your MySQL server.
   - Import the `schema.sql` file located in the `database` directory to create the necessary tables.
3. **Configure database connection**:
   - Open `php/connection/config.php` and update the database credentials as needed.

## Usage

- To register a new user, send a POST request to `php/register.php` with the following JSON payload:

```json
{
    "student_id": "your_student_id",
    "email": "your_email@example.com",
    "password": "your_password",
    "confirm_password": "your_password"
}
```

- The API will validate the input, check for existing users, hash the password, and store the user details in the database.

## Database Schema

The database includes a `users` table with the following structure:

```sql
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
```

## Contributing

Feel free to submit issues or pull requests for any improvements or bug fixes.