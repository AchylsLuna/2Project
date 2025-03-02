<?php
include 'connection.php';

$data = json_decode(file_get_contents("php://input"), true);

if (isset($data['username']) && isset($data['password'])) {
    $username = $data['username'];
    $password = $data['password'];

    $stmt = $conn->prepare("SELECT id FROM users WHERE username = ? AND password = ?");
    $stmt->bind_param("ss", $username, $password);
    $stmt->execute();
    $stmt->store_result();

    if ($stmt->num_rows > 0) {
        $stmt->bind_result($user_id);
        $stmt->fetch();
        echo json_encode(["success" => true, "user_id" => $user_id, "message" => "Login successful"]);
    } else {
        echo json_encode(["success" => false, "message" => "Invalid credentials"]);
    }

    $stmt->close();
} else {
    echo json_encode(["success" => false, "message" => "Missing username or password"]);
}

$conn->close();
?>
