<?php
include 'connection.php';

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $user_id = $_POST['user_id'];
    $date = $_POST['date'];
    $time_in = $_POST['time_in'];
    $time_out = $_POST['time_out'];

    $sql = "INSERT INTO user_logs (user_id, date, time_in, time_out) VALUES (?, ?, ?, ?)";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("isss", $user_id, $date, $time_in, $time_out);

    if ($stmt->execute()) {
        echo json_encode(["success" => true, "message" => "Time logged successfully"]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to log time"]);
    }

    $stmt->close();
}

$conn->close();
?>
