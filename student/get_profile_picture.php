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

    $stmt = $pdo->prepare("SELECT id, profile_picture FROM students WHERE session_token = ?");
    $stmt->execute([$sessionToken]);
    $student = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$student) {
        throw new Exception("Invalid session token", 401);
    }

    if (empty($student['profile_picture'])) {
        echo json_encode([
            "success" => true,
            "profilePicture" => null
        ]);
    } else {
        // Encode the binary data as base64
        $base64Image = base64_encode($student['profile_picture']);
        echo json_encode([
            "success" => true,
            "profilePicture" => "data:image/jpeg;base64,$base64Image" // Adjust MIME type if needed
        ]);
    }

} catch (Exception $e) {
    http_response_code($e->getCode() ?: 500);
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
    exit();
}
?>