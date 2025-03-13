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

    // Fetch all duty logs (both approved and pending)
    $stmt = $pdo->prepare("SELECT id, duty_date, time_in, time_out, status FROM duty_logs WHERE student_id = ? ORDER BY duty_date DESC, time_in DESC");
    $stmt->execute([$student_id]);
    $duty_logs = $stmt->fetchAll(PDO::FETCH_ASSOC);

    foreach ($duty_logs as &$log) {
        $log['duration_hours'] = ($log['time_out']) ? round((strtotime($log['time_out']) - strtotime($log['time_in'])) / 3600, 2) : null;
    }

    echo json_encode(["success" => true, "duty_logs" => $duty_logs ?: []]);

} catch (Exception $e) {
    http_response_code($e->getCode() ?: 500);
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
    exit();
}
?>
