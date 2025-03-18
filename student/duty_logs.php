<?php
require_once '../config/database.php';

header("Content-Type: application/json");

try {
    $headers = getallheaders();
    $authHeader = $headers['Authorization'] ?? '';
    $token = str_replace('Bearer ', '', trim($authHeader));

    if (empty($token)) {
        throw new Exception("Missing authorization token", 401);
    }

    $is_admin = false;
    $student_id = null;

    // Check admin token
    $stmt = $pdo->prepare("SELECT id FROM admin WHERE session_token = ?");
    $stmt->execute([$token]);
    $admin = $stmt->fetch(PDO::FETCH_ASSOC);
    if ($admin) {
        $is_admin = true;
    } else {
        // Check student token
        $stmt = $pdo->prepare("SELECT id FROM students WHERE session_token = ?");
        $stmt->execute([$token]);
        $student = $stmt->fetch(PDO::FETCH_ASSOC);
        if ($student) {
            $student_id = $student['id'];
        } else {
            throw new Exception("Invalid session token", 401);
        }
    }

    if ($is_admin) {
        $stmt = $pdo->prepare("
            SELECT dl.id, dl.student_id, DATE(dl.time_in) AS duty_date, dl.time_in, dl.time_out, 
                   TIMESTAMPDIFF(HOUR, dl.time_in, dl.time_out) AS hours_worked, 
                   dl.total_hours, dl.status, s.name AS student_name
            FROM duty_logs dl
            INNER JOIN students s ON dl.student_id = s.id
            WHERE dl.status = 'Pending'
            ORDER BY dl.time_in DESC
        ");
        $stmt->execute();
        $duty_logs = $stmt->fetchAll(PDO::FETCH_ASSOC);
    } else {
        $stmt = $pdo->prepare("
            SELECT time_in, time_out, total_hours, status 
            FROM duty_logs 
            WHERE student_id = ? 
            ORDER BY time_in DESC
        ");
        $stmt->execute([$student_id]);
        $duty_logs = $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

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