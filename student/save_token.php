<?php
header('Content-Type: text/plain');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);
    if (isset($data['fcm_token'])) {
        $token = $data['fcm_token'];
        // Save the token to a file
        file_put_contents('fcm_token.txt', $token);
        echo "Token saved successfully";
    } else {
        echo "No token provided";
    }
} else {
    echo "Invalid request method";
}
?>