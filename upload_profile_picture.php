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

    $stmt = $pdo->prepare("SELECT id FROM students WHERE session_token = ?");
    $stmt->execute([$sessionToken]);
    $student = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$student) {
        throw new Exception("Invalid session token", 401);
    }

    $student_id = $student['id'];

    if (!isset($_FILES['profile_picture'])) {
        throw new Exception("No image uploaded", 400);
    }

    $file = $_FILES['profile_picture'];
    $uploadDir = '../uploads/profile_pictures/';
    if (!file_exists($uploadDir)) {
        mkdir($uploadDir, 0777, true);
    }

    $fileName = $student_id . '_' . time() . '.' . pathinfo($file['name'], PATHINFO_EXTENSION);
    $filePath = $uploadDir . $fileName;

    if (move_uploaded_file($file['tmp_name'], $filePath)) {
        $fileUrl = "http://yourserver.com/uploads/profile_pictures/$fileName";
        
        $stmt = $pdo->prepare("UPDATE students SET profile_picture = ? WHERE id = ?");
        $stmt->execute([$fileUrl, $student_id]);

        echo json_encode([
            "success" => true,
            "profilePictureUrl" => $fileUrl
        ]);
    } else {
        throw new Exception("Failed to upload image", 500);
    }

} catch (Exception $e) {
    http_response_code($e->getCode() ?: 500);
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
    exit();
}
?>