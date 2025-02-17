<?php
session_start();
include("config.php");

date_default_timezone_set('Asia/Manila');


$date = date("Y-m-d"); 
$time_in = "";
$time_out = "";


if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (isset($_SESSION['student_id'])) {
        echo "student_id: " . $_SESSION['student_id'] . "<br>"; 
        $user_id = $_SESSION['student_id'];
    } elseif (isset($_SESSION['username'])) {
        echo "username: " . $_SESSION['username'] . "<br>";
        $user_id = $_SESSION['username'];
    } else {
        echo "User not logged in. Please log in to continue.";
        exit();
    }

    // Handle Time In
    if (isset($_POST['set_time_in'])) {
        $time_in = date("H:i"); 
        $_SESSION['time_in'] = $time_in;
    }

    // Handle Time Out
    if (isset($_POST['set_time_out'])) {
        $time_out = date("H:i");
        $_SESSION['time_out'] = $time_out;
    }

    if (isset($_POST['submit'])) {
        $time_in = $_SESSION['time_in'] ?? "";
        $time_out = $_SESSION['time_out'] ?? "";


        if ($time_in || $time_out) {
            $stmt = $conn->prepare("INSERT INTO time_logs (user_id, date, time_in, time_out) VALUES (?, ?, ?, ?)");
            $stmt->bind_param("ssss", $user_id, $date, $time_in, $time_out); 

            if ($stmt->execute()) {
                echo "Updated: Time log saved successfully!<br>";
                unset($_SESSION['time_in']);
                unset($_SESSION['time_out']);
            } else {
                echo "Error saving time log: " . $conn->error;
            }

            // Close the statement
            $stmt->close();
        } else {
            echo "No time data to save.";
        }
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Time Log Form</title>
</head>
<body>
    <h2>Log Time</h2>
    <form method="post" action="">
        <label for="date">Date:</label>
        <input type="date" id="date" name="date" value="<?php echo $date; ?>" readonly><br><br>
        
        <label for="time_in">Time In:</label>
        <input type="time" id="time_in" name="time_in" value="<?php echo $_SESSION['time_in'] ?? ''; ?>" readonly><br>
        <button type="submit" name="set_time_in">Set Time In</button><br>
        
        <label for="time_out">Time Out:</label>
        <input type="time" id="time_out" name="time_out" value="<?php echo $_SESSION['time_out'] ?? ''; ?>" readonly><br>
        <button type="submit" name="set_time_out">Set Time Out</button><br> 
        
        <input type="submit" name="submit" value="Submit">
    </form>

    <h3>Current Log Data:</h3>
    <p>Date: <?php echo $date; ?></p>
    <p>Time In: <?php echo $_SESSION['time_in'] ?? "Not Set"; ?></p>
    <p>Time Out: <?php echo $_SESSION['time_out'] ?? "Not Set"; ?></p>
    <button onclick="window.location.href='homepage.php'">Back</button>
</body>
</html>