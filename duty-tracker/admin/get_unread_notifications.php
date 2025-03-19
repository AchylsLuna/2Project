<?php
require_once '../config/database.php';
session_start();

if (!isset($_SESSION['admin_id'])) {
    exit(json_encode(['unread_count' => 0]));
}

$stmt = $pdo->prepare("SELECT COUNT(*) AS unread_count FROM notifications WHERE role = 'Admin' AND is_read = FALSE");
$stmt->execute();
$unread_count = $stmt->fetch(PDO::FETCH_ASSOC)['unread_count'];

header('Content-Type: application/json');
echo json_encode(['unread_count' => $unread_count]);
exit();