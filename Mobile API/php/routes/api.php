<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once "../connection/config.php";
// Database connection
$hostName = "localhost";
$dbUser = "root";
$dbPassword = "";
$dbName = "scholarly";

$conn = new mysqli($hostName, $dbUser, $dbPassword, $dbName);

if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

// Handle API Requests
if ($_SERVER['REQUEST_METHOD'] == "POST") {
    $action = isset($_GET['action']) ? $_GET['action'] : '';

    // Get JSON Input
    $data = json_decode(file_get_contents("php://input"), true);

    if (!$data) {
        http_response_code(400);
        echo json_encode(["status" => "error", "message" => "Invalid JSON input"]);
        exit;
    }

    // Handle Registration
    if ($action == "register") {
        $student_id = $data['student_id'] ?? '';
        $email = $data['email'] ?? '';
        $password = $data['password'] ?? '';

        // Validate Input
        if (empty($student_id) || empty($email) || empty($password)) {
            http_response_code(400);
            echo json_encode(["status" => "error", "message" => "All fields are required"]);
            exit;
        }

        // Check if email or student_id already exists
        $checkQuery = $conn->prepare("SELECT id FROM users WHERE student_id = ? OR email = ?");
        $checkQuery->bind_param("ss", $student_id, $email);
        $checkQuery->execute();
        $checkQuery->store_result();

        if ($checkQuery->num_rows > 0) {
            http_response_code(409);
            echo json_encode(["status" => "error", "message" => "Student ID or Email already exists"]);
            exit;
        }
        $checkQuery->close();

        // Hash password and insert new user
        $hashedPassword = password_hash($password, PASSWORD_DEFAULT);
        $query = $conn->prepare("INSERT INTO users (student_id, email, password) VALUES (?, ?, ?)");
        $query->bind_param("sss", $student_id, $email, $hashedPassword);

        if ($query->execute()) {
            http_response_code(201);
            echo json_encode(["status" => "success", "message" => "Registration successful"]);
        } else {
            http_response_code(500);
            echo json_encode(["status" => "error", "message" => "Registration failed"]);
        }
        $query->close();
    }

    // Handle Login
    elseif ($action == "login") {
        $email = $data['email'] ?? '';
        $password = $data['password'] ?? '';

        // Validate Input
        if (empty($email) || empty($password)) {
            http_response_code(400);
            echo json_encode(["status" => "error", "message" => "All fields are required"]);
            exit;
        }

        // Check if user exists
        $query = $conn->prepare("SELECT id, password FROM users WHERE email = ?");
        $query->bind_param("s", $email);
        $query->execute();
        $query->store_result();

        if ($query->num_rows > 0) {
            $query->bind_result($id, $hashedPassword);
            $query->fetch();

            // Verify password
            if (password_verify($password, $hashedPassword)) {
                http_response_code(200);
                echo json_encode(["status" => "success", "message" => "Login successful"]);
            } else {
                http_response_code(401);
                echo json_encode(["status" => "error", "message" => "Incorrect password"]);
            }
        } else {
            http_response_code(404);
            echo json_encode(["status" => "error", "message" => "User not found"]);
        }
        $query->close();
    }

    else {
        http_response_code(400);
        echo json_encode(["status" => "error", "message" => "Invalid request"]);
    }
}

$conn->close();
?>
