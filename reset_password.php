<?php
require_once '../config/database.php';
header("Content-Type: application/json");

$response = ['success' => false, 'message' => ''];

try {
    if ($_SERVER["REQUEST_METHOD"] !== "POST") {
        throw new Exception("Invalid request method", 405);
    }

    $data = json_decode(file_get_contents("php://input"), true);

    if (empty($data['email']) || empty($data['student_id']) || empty($data['new_password'])) {
        throw new Exception("Please provide email, student ID, and new password", 400);
    }

    $email = trim($data['email']);
    $student_id = trim($data['student_id']);
    $new_password = trim($data['new_password']);

    // Check if student exists
    $stmt = $pdo->prepare("SELECT * FROM students WHERE email = ? AND student_id = ?");
    $stmt->execute([$email, $student_id]);
    $student = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$student) {
        throw new Exception("Invalid email or student ID", 404);
    }

    // Hash the new password
    $hashedPassword = password_hash($new_password, PASSWORD_DEFAULT);

    // Update password in the database
    $updateStmt = $pdo->prepare("UPDATE students SET password = ? WHERE email = ? AND student_id = ?");
    if ($updateStmt->execute([$hashedPassword, $email, $student_id])) {
        $response['success'] = true;
        $response['message'] = "Password reset successfully!";
    } else {
        throw new Exception("Failed to reset password. Try again later.", 500);
    }
} catch (PDOException $e) {
    $response['message'] = "Database error: " . $e->getMessage();
} catch (Exception $e) {
    $response['message'] = $e->getMessage();
    http_response_code($e->getCode() ?: 500);
}

echo json_encode($response);
exit();
?>
