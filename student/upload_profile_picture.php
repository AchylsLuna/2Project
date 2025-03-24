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

    // Ensure PDO is connected
    if (!$pdo) {
        throw new Exception("Database connection failed", 500);
    }

    $stmt = $pdo->prepare("SELECT id FROM students WHERE session_token = ?");
    $stmt->execute([$sessionToken]);
    $student = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$student) {
        throw new Exception("Invalid session token", 401);
    }

    $student_id = $student['id'];

    // Debug: Log incoming file data
    file_put_contents('upload_debug.log', "FILES: " . print_r($_FILES, true) . "\n", FILE_APPEND);

    if (!isset($_FILES['profile_picture'])) {
        throw new Exception("No image uploaded", 400);
    }

    $file = $_FILES['profile_picture'];
    $allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
    $maxSize = 5 * 1024 * 1024; // 5MB

    if (!in_array($file['type'], $allowedTypes)) {
        throw new Exception("Invalid image type. Only JPEG, PNG, and GIF are allowed.", 400);
    }
    if ($file['size'] > $maxSize) {
        throw new Exception("Image size exceeds 5MB limit.", 400);
    }

    // Read the file content into a binary string
    $imageData = file_get_contents($file['tmp_name']);
    if ($imageData === false) {
        throw new Exception("Failed to read image data", 500);
    }

    // Update the database with the image binary data
    $stmt = $pdo->prepare("UPDATE students SET profile_picture = ? WHERE id = ?");
    $stmt->execute([$imageData, $student_id]);

    echo json_encode([
        "success" => true,
        "message" => "Profile picture uploaded successfully"
    ]);

} catch (Exception $e) {
    $code = is_numeric($e->getCode()) ? (int)$e->getCode() : 500;
    http_response_code($code);
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
    exit();
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Database error: " . $e->getMessage()]);
    exit();
}
?>