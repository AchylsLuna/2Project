<?php
require_once '../config/database.php';

header("Content-Type: application/json");

try {
    $headers = getallheaders();
    $authHeader = $headers['Authorization'] ?? '';
    $sessionToken = str_replace('Bearer ', '', trim($authHeader));

    if (empty($sessionToken)) {
        throw new Exception("Missing authorization token", 401);
    }

    // Validate student session token
    $stmt = $pdo->prepare("SELECT id FROM students WHERE session_token = ?");
    $stmt->execute([$sessionToken]);
    $student = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$student) {
        throw new Exception("Invalid session token", 401);
    }

    $student_id = $student['id'];

    // Fetch duty logs with relevant fields
    $stmt = $pdo->prepare("SELECT time_in, time_out, total_hours, status 
                           FROM duty_logs 
                           WHERE student_id = ? 
                           ORDER BY time_in DESC");
    $stmt->execute([$student_id]);
    $duty_logs = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Prepare response
    $response = [
        "success" => true,
        "logs" => $duty_logs ?: []
    ];

    echo json_encode($response);

} catch (Exception $e) {
    http_response_code($e->getCode() ?: 500);
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
    exit();
}
?>