<?php
session_start();
require_once '../config/database.php';

if (!isset($_SESSION['admin_id'])) {
    header('Location: login.php');
    exit();
}

if (isset($_GET['student_id'])) {
    $student_id = $_GET['student_id'];

    // Delete associated duty logs
    $stmt = $pdo->prepare("DELETE FROM duty_logs WHERE student_id = :student_id");
    $stmt->execute(['student_id' => $student_id]);

    // Delete the student
    $stmt = $pdo->prepare("DELETE FROM students WHERE student_id = :student_id");
    $stmt->execute(['student_id' => $student_id]);

    header('Location: student_profiles.php');
    exit();
}
?>