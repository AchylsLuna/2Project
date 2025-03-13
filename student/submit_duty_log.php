<?php
require_once '../config/database.php';

header("Content-Type: application/json");
$response = ['success' => false, 'message' => ''];

try {
    $headers = getallheaders();
    $authHeader = $headers['Authorization'] ?? '';
    $sessionToken = str_replace('Bearer ', '', trim($authHeader));

    if (empty($sessionToken)) {
        throw new Exception("Missing authorization token", 401);
    }

    if (!$pdo) {
        throw new Exception("Database connection failed", 500);
    }

    // Get student ID from session token
    $stmt = $pdo->prepare("SELECT id FROM students WHERE session_token = ?");
    $stmt->execute([$sessionToken]);
    $student = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$student) {
        throw new Exception("Invalid session token", 401);
    }

    $data = json_decode(file_get_contents('php://input'), true);

    if (json_last_error() !== JSON_ERROR_NONE) {
        throw new Exception("Invalid JSON format", 400);
    }

    $requiredFields = ['duty_date', 'time_in', 'time_out'];
    foreach ($requiredFields as $field) {
        if (empty($data[$field])) {
            throw new Exception("Missing required field: $field", 400);
        }
    }

    $dutyDate = filter_var(trim($data['duty_date']), FILTER_SANITIZE_STRING);
    $timeIn = filter_var(trim($data['time_in']), FILTER_SANITIZE_STRING);
    $timeOut = filter_var(trim($data['time_out']), FILTER_SANITIZE_STRING);

    if (!preg_match('/^\d{4}-\d{2}-\d{2}$/', $dutyDate)) {
        throw new Exception("Invalid date format. Use YYYY-MM-DD", 400);
    }

    if (!preg_match('/^\d{2}:\d{2}:\d{2}$/', $timeIn) || 
        !preg_match('/^\d{2}:\d{2}:\d{2}$/', $timeOut)) {
        throw new Exception("Time format must be HH:MM:SS", 400);
    }

    // Calculate hours worked for this entry
    $timeInObj = new DateTime($timeIn);
    $timeOutObj = new DateTime($timeOut);
    $interval = $timeInObj->diff($timeOutObj);
    
    $hoursWorkedEntry = $interval->h + ($interval->i / 60); // Convert minutes to fraction of an hour

    // Insert new duty log
    $stmt = $pdo->prepare("INSERT INTO duty_logs (student_id, duty_date, time_in, time_out, total_hours)
                           VALUES (?, ?, ?, ?, ?)");
    $success = $stmt->execute([$student['id'], $dutyDate, $timeIn, $timeOut, $hoursWorkedEntry]);

    if (!$success) {
        throw new Exception("Failed to submit duty log", 500);
    }

    // Calculate total hours worked on the current day
    $stmt = $pdo->prepare("SELECT SUM(total_hours) AS hours_worked FROM duty_logs 
                           WHERE student_id = ? AND duty_date = ?");
    $stmt->execute([$student['id'], $dutyDate]);
    $result = $stmt->fetch(PDO::FETCH_ASSOC);
    $hoursWorked = $result['hours_worked'] ?? 0;

    // Calculate cumulative total hours across all logs
    $stmt = $pdo->prepare("SELECT SUM(total_hours) AS total_hours FROM duty_logs 
                           WHERE student_id = ?");
    $stmt->execute([$student['id']]);
    $totalHours = $stmt->fetch(PDO::FETCH_ASSOC)['total_hours'] ?? 0;

    // Response
    $response['success'] = true;
    $response['message'] = "Duty log submitted successfully";
    $response['hours_worked'] = $hoursWorked;
    $response['total_hours'] = $totalHours;

    http_response_code(200);

} catch (Exception $e) {
    $response['message'] = $e->getMessage();
    http_response_code($e->getCode() ?: 500);
}

echo json_encode($response);
exit();
