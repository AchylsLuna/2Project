<?php
session_start();
require_once '../config/database.php';

if (!isset($_SESSION['admin_id'])) {
    header("Location: login.php");
    exit();
}

$admin_id = $_SESSION['admin_id'];

// Fetch notifications for admin (new duty log submissions)
$stmt = $pdo->prepare("
    SELECT n.id, n.message, n.is_read, n.created_at, s.name AS student_name, s.student_id AS student_id
    FROM notifications n
    INNER JOIN students s ON n.user_id = s.id
    WHERE n.role = 'Admin' AND n.user_id IN (SELECT id FROM students)
    ORDER BY n.created_at DESC
");
$stmt->execute();
$notifications = $stmt->fetchAll(PDO::FETCH_ASSOC);
?>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Notifications</title>
    <link rel="stylesheet" href="../assets/student.css">
</head>

<body>
    <div class="notifications-container">
        <header>
            <h2>Duty Log Submission Notifications</h2>
            <a href="dashboard.php">Back to Dashboard</a>
        </header>

        <section class="notifications-list">
            <?php if ($notifications): ?>
            <ul>
                <?php foreach ($notifications as $notification): ?>
                <li data-status="<?php echo $notification['is_read'] ? 'read' : 'unread'; ?>">
                    <strong>Student Name:</strong> <?php echo htmlspecialchars($notification['student_name']); ?><br>
                    <strong>Student ID:</strong> <?php echo htmlspecialchars($notification['student_id']); ?><br>
                    <strong>Message:</strong> <?php echo htmlspecialchars($notification['message']); ?><br>
                    <strong>Received:</strong> <?php echo date('Y-m-d h:i A', strtotime($notification['created_at'])); ?><br>
                    <strong>Status:</strong> <span><?php echo $notification['is_read'] ? 'Read' : 'Unread'; ?></span>
                </li>
                <?php endforeach; ?>
            </ul>
            <?php else: ?>
            <p>No duty log submission notifications.</p>
            <?php endif; ?>
        </section>
    </div>
</body>

</html>