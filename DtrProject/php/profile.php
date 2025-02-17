<?php
session_start();
include("config.php");

// if the user is logged in via either student_id or username
if (!isset($_SESSION['student_id']) && !isset($_SESSION['username'])) {
    echo "User not logged in.<br>";
    exit(); // Stop further execution if no session found
}

if (isset($_SESSION['student_id'])) {
    // Fetch user data based on student id
    $user_id = $_SESSION['student_id'];
    $stmt = $conn->prepare("SELECT * FROM users WHERE student_id = ?");
    $stmt->bind_param("s", $user_id);
} else {
    // Fetch user data based on username
    $user_id = $_SESSION['username'];
    $stmt = $conn->prepare("SELECT * FROM users WHERE username = ?");
    $stmt->bind_param("s", $user_id);
}

$stmt->execute();
$result = $stmt->get_result();
$user_data = $result->fetch_assoc();
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Update Profile</title>
</head>
<body>

<?php if ($user_data): ?>
    <h2>Update Your Profile</h2>
    <form method="post" action="process_type.php">
        <p>Scholarship Type</p>
        <select name="scholarship_type">
            <option value="25%" <?php if ($user_data['scholarship_type'] == '25%') echo 'selected'; ?>>25%</option>
            <option value="50%" <?php if ($user_data['scholarship_type'] == '50%') echo 'selected'; ?>>50%</option>
            <option value="75%" <?php if ($user_data['scholarship_type'] == '75%') echo 'selected'; ?>>75%</option>
        </select>

        <p>Duty Status</p>
        <select name="duty_status">
            <option value="Active" <?php if ($user_data['duty_status'] == 'Active') echo 'selected'; ?>>Active</option>
            <option value="Inactive" <?php if ($user_data['duty_status'] == 'Inactive') echo 'selected'; ?>>Inactive</option>
        </select>

        <p>Schedule</p>
        <select name="schedule">
            <option value="Morning" <?php if ($user_data['schedule'] == 'Morning') echo 'selected'; ?>>Morning</option>
            <option value="Afternoon" <?php if ($user_data['schedule'] == 'Afternoon') echo 'selected'; ?>>Afternoon</option>
            <option value="Evening" <?php if ($user_data['schedule'] == 'Evening') echo 'selected'; ?>>Evening</option>
        </select>

        <p>Expert Teacher</p>
        <select name="expert_teacher">
            <option value="Josh Dacasin" <?php if ($user_data['expert_teacher'] == 'Josh Dacasin') echo 'selected'; ?>>Josh Dacasin</option>
            <option value="Reynard Visperas" <?php if ($user_data['expert_teacher'] == 'Reynard Visperas') echo 'selected'; ?>>Reynard Visperas</option>
            <option value="Veronica Canlas" <?php if ($user_data['expert_teacher'] == 'Veronica Canlas') echo 'selected'; ?>>Veronica Canlas</option>
        </select>

        <p>Subject Code</p>
        <select name="subject_code">
            <option value="ITE300" <?php if ($user_data['subject_code'] == 'ITE300') echo 'selected'; ?>>ITE300</option>
            <option value="ITE393" <?php if ($user_data['subject_code'] == 'ITE393') echo 'selected'; ?>>ITE393</option>
            <option value="ITE400" <?php if ($user_data['subject_code'] == 'ITE400') echo 'selected'; ?>>ITE400</option>
        </select>

        <br>
        <input type="submit" value="Update Profile">
        <button href="homepage.php">Back</button>
    </form>
<?php else: ?>
    <p>No user data found.</p>
<?php endif; ?>

</body>
</html>

<?php
// Process the form if its submitted
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // Sanitize input data
    $scholarship_type = htmlspecialchars($_POST['scholarship_type']);
    $duty_status = htmlspecialchars($_POST['duty_status']);
    $schedule = htmlspecialchars($_POST['schedule']);
    $expert_teacher = htmlspecialchars($_POST['expert_teacher']);
    $subject_code = htmlspecialchars($_POST['subject_code']);

    // Validate input fields
    if (empty($scholarship_type) || empty($duty_status) || empty($schedule) || empty($expert_teacher) || empty($subject_code)) {
        die("All fields are required.");
    }

    // Update user profile
    if (isset($_SESSION['student_id'])) {
        $stmt = $conn->prepare("UPDATE users SET scholarship_type = ?, duty_status = ?, schedule = ?, expert_teacher = ?, subject_code = ? WHERE student_id = ?");
        $stmt->bind_param("ssssss", $scholarship_type, $duty_status, $schedule, $expert_teacher, $subject_code, $_SESSION['student_id']);
    } else {
        $stmt = $conn->prepare("UPDATE users SET scholarship_type = ?, duty_status = ?, schedule = ?, expert_teacher = ?, subject_code = ? WHERE username = ?");
        $stmt->bind_param("ssssss", $scholarship_type, $duty_status, $schedule, $expert_teacher, $subject_code, $_SESSION['username']);
    }

    if ($stmt->execute()) {
        $stmt->close();
        echo "Profile updated successfully!";
        header("Location: homepage.php");
        exit();
    } else {
        echo "Error updating profile: " . $conn->error;
    }
}
?>
