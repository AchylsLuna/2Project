<?php
require_once '../config/database.php';
header("Content-Type: application/json");

$response = ['success' => false, 'message' => ''];

try {
    // Verify session token
    $headers = getallheaders();
    $token = str_replace('Bearer ', '', $headers['Authorization'] ?? '');
    
    $stmt = $pdo->prepare("SELECT id FROM students WHERE session_token = ?");
    $stmt->execute([$token]);
    $user = $stmt->fetch();

    if (!$user) {
        throw new Exception("Unauthorized", 401);
    }

    // File upload handling
    if (empty($_FILES['profile_pic'])) {
        throw new Exception("No file uploaded", 400);
    }

    $file = $_FILES['profile_pic'];
    
    // Validate file
    $allowedTypes = ['image/jpeg', 'image/png'];
    $maxSize = 2 * 1024 * 1024; // 2MB

    if (!in_array($file['type'], $allowedTypes)) {
        throw new Exception("Only JPG/PNG files allowed", 400);
    }

    if ($file['size'] > $maxSize) {
        throw new Exception("File size exceeds 2MB limit", 400);
    }

    // Generate unique filename
    $extension = pathinfo($file['name'], PATHINFO_EXTENSION);
    $filename = "profile_{$user['id']}." . $extension;
    $uploadPath = "../uploads/" . $filename;

    if (!move_uploaded_file($file['tmp_name'], $uploadPath)) {
        throw new Exception("Failed to save file", 500);
    }

    // Update database
    $stmt = $pdo->prepare("UPDATE students SET profile_pic = ? WHERE id = ?");
    $stmt->execute([$filename, $user['id']]);

    $response = [
        'success' => true,
        'message' => 'Profile picture updated',
        'profile_pic' => $filename
    ];

} catch (Exception $e) {
    $response['message'] = $e->getMessage();
    http_response_code($e->getCode() ?: 500);
}

echo json_encode($response);