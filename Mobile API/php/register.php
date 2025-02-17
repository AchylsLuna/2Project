<?php
session_start();
include '../connection/config.php'; // Ensure correct path to config

// Enable debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");

// Check if request is JSON
$isJsonRequest = isset($_SERVER["CONTENT_TYPE"]) && strpos($_SERVER["CONTENT_TYPE"], "application/json") !== false;

if ($isJsonRequest) {
    // Read input
    $inputJSON = file_get_contents("php://input");
    $data = json_decode($inputJSON, true);

    if (!$data) {
        echo json_encode(["status" => "error", "message" => "Invalid JSON input"]);
        exit();
    }

    // Debugging output
    file_put_contents("debug.log", "Received Data: " . print_r($data, true) . PHP_EOL, FILE_APPEND);

    $student_id = $data['student_id'] ?? '';
    $email = $data['email'] ?? '';
    $password = $data['password'] ?? '';
    $confirm_password = $data['confirm_password'] ?? '';

    // Validate input
    if (empty($student_id) || empty($email) || empty($password) || empty($confirm_password)) {
        echo json_encode(["status" => "error", "message" => "All fields are required"]);
        exit();
    }

    if ($password !== $confirm_password) {
        echo json_encode(["status" => "error", "message" => "Passwords do not match"]);
        exit();
    }

    // Check if email exists
    $checkEmail = $conn->prepare("SELECT * FROM users WHERE email = ?");
    $checkEmail->bind_param("s", $email);
    $checkEmail->execute();
    $result = $checkEmail->get_result();

    if ($result->num_rows > 0) {
        echo json_encode(["status" => "error", "message" => "Email already exists"]);
        exit();
    }

    // Hash password and insert user
    $hashedPassword = password_hash($password, PASSWORD_DEFAULT);
    $insertQuery = $conn->prepare("INSERT INTO users (student_id, email, password) VALUES (?, ?, ?)");
    $insertQuery->bind_param("sss", $student_id, $email, $hashedPassword);

    if ($insertQuery->execute()) {
        echo json_encode(["status" => "success", "message" => "Registration successful"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Registration failed: " . $conn->error]);
    }
    exit();
}

echo json_encode(["status" => "error", "message" => "Invalid request"]);
?>
