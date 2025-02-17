<?php
session_start();
include("config.php");

$user_found = false; //if user data is found
$username = ""; // Default username

if (isset($_SESSION['student_id'])) {
    $student_id = $_SESSION['student_id'];
    $query = mysqli_query($conn, "SELECT * FROM `users` WHERE student_id='$student_id' LIMIT 1");

    if ($query && mysqli_num_rows($query) > 0) {
        $row = mysqli_fetch_array($query);
        $username = htmlspecialchars($row['username']);
        $user_found = true;
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Homepage</title>
</head>
<body>
    <div style="text-align:center; padding:15%;">
        <p style="font-size:50px; font-weight:bold;">
            Hello <?php echo $username; ?>
        </p>

        <?php if ($user_found) { ?>
        <p style="font-size:20px; font-weight:bold;">
            Scholarship Type: <?php echo htmlspecialchars($row['scholarship_type']); ?><br>
            Duty Status: <?php echo htmlspecialchars($row['duty_status']); ?><br>
            Schedule: <?php echo htmlspecialchars($row['schedule']); ?><br>
            Expert Teacher: <?php echo htmlspecialchars($row['expert_teacher']); ?><br>
            Subject Code: <?php echo htmlspecialchars($row['subject_code']); ?>
        </p>
        <?php } ?>

        <a href="type.php">Scholarship</a><br>
        <a href="profile.php">Update Profile</a><br>
        <a href="timeStamp.php">Duty Status</a><br>
        <a href="logout.php">Logout</a>
    </div>
</body>
</html>
