<?php
session_start();
require_once '../config/database.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['duty_id'])) {
    $duty_id = $_POST['duty_id'];

    $stmt = $pdo->prepare("DELETE FROM duty_logs WHERE id = ?");
    $stmt->execute([$duty_id]);

    if ($stmt->rowCount() > 0) {
        echo json_encode(['success' => true]);
    } else {
        echo json_encode(['success' => false]);
    }
}
?>