<?php
require_once '../config/database.php';
header("Content-Type: application/json");

$response = ['success' => false, 'message' => ''];

try {
    if ($_SERVER["REQUEST_METHOD"] != "POST") {
        throw new Exception("Invalid request method", 405);
    }

    $data = json_decode(file_get_contents("php://input"), true);
    
    if (empty($data['student_id']) || empty($data['password'])) {
        throw new Exception("Please provide student ID and password", 400);
    }

    $student_id = trim($data['student_id']);
    $password = trim($data['password']);

    $stmt = $pdo->prepare("SELECT * FROM students WHERE student_id = ?");
    $stmt->execute([$student_id]);
    $student = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$student || !password_verify($password, $student['password'])) {
        throw new Exception("Invalid credentials", 401);
    }

    // Generate and store token
    $token = bin2hex(random_bytes(32));
    $updateStmt = $pdo->prepare("UPDATE students SET session_token = ? WHERE id = ?");
    $updateStmt->execute([$token, $student['id']]);

    $response = [
        'success' => true,
        'message' => 'Login successful',
        'token' => $token,
        'student' => [
            'id' => $student['id'],
            'student_id' => $student['student_id'],
            'name' => $student['name'],
            'email' => $student['email'],
            'scholarship_type' => $student['scholarship_type'], // Must match DB column name
            'course' => $student['course'],
            'department' => $student['department'],
            'hk_duty_status' => $student['hk_duty_status']
        ]
    ];

} catch (PDOException $e) {
    $response['message'] = "Database error: " . $e->getMessage();
} catch (Exception $e) {
    $response['message'] = $e->getMessage();
    http_response_code($e->getCode() ?: 500);
}

echo json_encode($response);
exit();
?>