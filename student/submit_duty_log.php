<?php
session_start();
require_once '../config/database.php';  // Database connection

header("Content-Type: application/json");

$response = array();

// Ensure the user is logged in and has a student ID in session
if (!isset($_SESSION['student_id'])) {
    $response['success'] = false;
    $response['message'] = "Unauthorized: Please log in.";
    echo json_encode($response);
    exit();
}

$student_id = $_SESSION['student_id']; // Get student_id from session

// Get the JSON input
$data = json_decode(file_get_contents("php://input"), true);

// Validate required fields
if (!isset($data['duty_date'], $data['time_in'], $data['time_out'])) {
    $response['success'] = false;
    $response['message'] = "All fields are required.";
    echo json_encode($response);
    exit();
}

$duty_date = trim($data['duty_date']);
$time_in = trim($data['time_in']);
$time_out = trim($data['time_out']);

// Insert duty log with student_id from session
$stmt = $pdo->prepare("INSERT INTO duty_logs (student_id, duty_date, time_in, time_out) VALUES (?, ?, ?, ?)");
$success = $stmt->execute([$student_id, $duty_date, $time_in, $time_out]);

if ($success) {
    $response['success'] = true;
    $response['message'] = "Duty log submitted successfully.";
    $response['student_id'] = $student_id; // Return student_id in response
} else {
    $response['success'] = false;
    $response['message'] = "Failed to submit duty log.";
}

echo json_encode($response);
?>
