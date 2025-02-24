<?php
session_start();

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include "connection/config.php";

if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

$input = json_decode(file_get_contents("php://input"), true);

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    // Check if this is a registration request
    if (isset($input["email"]) && isset($input["password"]) && isset($input["confirm_password"])) {
        
        $student_id = $input["student_id"] ?? '';
        $email = $input["email"];
        $password = $input["password"];
        $confirm_password = $input["confirm_password"];
        $duty_status = $input["duty_status"] ?? 'Select Duty Status';
        $scholarship_type = $input["scholarship_type"] ?? 'Select Scholar Type';
        $course = $input["course"] ?? 'Select Course';
        $year_level = $input["year_level"] ?? 'Select Year';

        // Validate required fields
        if (empty($student_id) || empty($email) || empty($password) || empty($confirm_password)) {
            echo json_encode(["success" => false, "message" => "All fields are required"]);
            exit;
        }

        // Check if passwords match
        if ($password !== $confirm_password) {
            echo json_encode(["success" => false, "message" => "Passwords do not match"]);
            exit;
        }

        // Check if student_id or email already exists
        $user = $conn->prepare("SELECT id FROM users WHERE student_id = ? OR email = ?");
        $user->bind_param("ss", $student_id, $email);
        $user->execute();
        $user->store_result();

        if ($user->num_rows > 0) {
            echo json_encode(["success" => false, "message" => "Student ID or Email already exists"]);
            exit;
        }
        $user->close();

        // Hash the password
        $hashed_password = password_hash($password, PASSWORD_DEFAULT);

        // Insert new user
        $stmt = $conn->prepare("INSERT INTO users (student_id, email, password, duty_status, scholarship_type, course, year_level) VALUES (?, ?, ?, ?, ?, ?, ?)");
        $stmt->bind_param("sssssss", $student_id, $email, $hashed_password, $duty_status, $scholarship_type, $course, $year_level);

        if ($stmt->execute()) {
            echo json_encode(["success" => true, "message" => "Registered Successfully"]);
        } else {
            echo json_encode(["success" => false, "message" => "Error: " . $stmt->error]);
        }

        $stmt->close();
    }
    // Check if this is an update request
    elseif (isset($input["student_id"]) && isset($input["duty_status"]) && isset($input["scholar_type"]) && isset($input["course"]) && isset($input["year_level"])) {
        
        $student_id = $input["student_id"];
        $duty_status = $input["duty_status"];
        $scholar_type = $input["scholar_type"];
        $course = $input["course"];
        $year_level = $input["year_level"];

        // Validate fields (excluding student_id)
        if (empty($duty_status) || empty($scholar_type) || empty($course) || empty($year_level)) {
            http_response_code(400);
            echo json_encode(["status" => "error", "message" => "All fields are required"]);
            exit;
        }

        // Update user details excluding student_id
        $query = $conn->prepare("UPDATE users SET duty_status = ?, scholar_type = ?, course = ?, year_level = ? WHERE student_id = ?");
        $query->bind_param("sssss", $duty_status, $scholar_type, $course, $year_level, $student_id);

        if ($query->execute()) {
            http_response_code(200);
            echo json_encode(["status" => "success", "message" => "Selection updated successfully"]);
        } else {
            http_response_code(500);
            echo json_encode(["status" => "error", "message" => "Failed to update selection"]);
        }
        $query->close();
    } else {
        http_response_code(400);
        echo json_encode(["status" => "error", "message" => "Invalid request"]);
    }
}

$conn->close();
?>
