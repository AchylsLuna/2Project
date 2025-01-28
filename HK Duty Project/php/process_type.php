<?php
session_start();
include("config.php");

// Debugging// Print session content
echo "<pre>";
print_r($_SESSION);
echo "</pre>";

if (!isset($_SESSION['student_id']) && !isset($_SESSION['username'])) {
    echo "User not logged in.<br>";
    exit(); // Stop execution if no session found
}

echo "Session started with ";

if (isset($_SESSION['student_id'])) {
    echo "student_id: " . $_SESSION['student_id'] . "<br>"; // Display student ID
    $user_id = $_SESSION['student_id'];
} else {
    echo "username: " . $_SESSION['username'] . "<br>"; // Display username
    $user_id = $_SESSION['username'];
}

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // input data
    $scholarship_type = htmlspecialchars($_POST['scholarship_type']);
    $duty_status = htmlspecialchars($_POST['duty_status']);
    $schedule = htmlspecialchars($_POST['schedule']);
    $expert_teacher = htmlspecialchars($_POST['expert_teacher']);
    $subject_code = htmlspecialchars($_POST['subject_code']);

    // validatation of input fields tite
    if (empty($scholarship_type) || empty($duty_status) || empty($schedule) || empty($expert_teacher) || empty($subject_code)) {
        die("All fields are required.");
    }

    // find the user if username or student id
    $stmt = $conn->prepare("UPDATE users SET scholarship_type = ?, duty_status = ?, schedule = ?, expert_teacher = ?, subject_code = ? WHERE student_id = ? OR username = ?");
    
    // If using username or student_id, it will pass validation
    if (isset($_SESSION['student_id'])) {
        $stmt->bind_param("sssssss", $scholarship_type, $duty_status, $schedule, $expert_teacher, $subject_code, $user_id, $user_id); // student_id
    } else {
        $stmt->bind_param("sssssss", $scholarship_type, $duty_status, $schedule, $expert_teacher, $subject_code, $user_id, $user_id); // username
    }

    if ($stmt->execute()) {
        // will close the statement after execution
        $stmt->close();
        echo "Profile updated successfully!";
        header("Location: homepage.php");
        exit(); // Stop script executio 
    } else {
        echo "Error updating profile: " . $conn->error;
    }
}
?>
