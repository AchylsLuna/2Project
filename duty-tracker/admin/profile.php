<?php
session_start();
require_once '../config/database.php';

// Check if admin is logged in
if (!isset($_SESSION['admin_id'])) {
    header('Location: login.php');
    exit();
}
$stmt = $pdo->prepare("SELECT COUNT(*) AS total_students FROM students");
$stmt->execute();
$total_students = $stmt->fetch(PDO::FETCH_ASSOC)['total_students'];

$stmt = $pdo->prepare("SELECT COUNT(*) AS pending_logs FROM duty_logs WHERE status = 'Pending'");
$stmt->execute();
$pending_logs = $stmt->fetch(PDO::FETCH_ASSOC)['pending_logs'];

// Check if the session variables exist before using them
$admin_name = isset($_SESSION['admin_name']) ? $_SESSION['admin_name'] : "Not Set";
$admin_email = isset($_SESSION['admin_email']) ? $_SESSION['admin_email'] : "Email Not Set";

// If you need to fetch more details from the database, do it here
$admin_id = $_SESSION['admin_id'];
$stmt = $pdo->prepare("SELECT * FROM admin WHERE id = ?");
$stmt->execute([$admin_id]);
$admin_data = $stmt->fetch(PDO::FETCH_ASSOC);

// If data was fetched successfully, update the variables
if ($admin_data) {
    $admin_name = $admin_data['name'];
    $admin_email = $admin_data['email'];
}
?>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HK DUTY TRACKER</title>
    <link rel="stylesheet" href="../assets/css/admin.css">
    <link rel="icon" href="../assets/image/icontitle.png">
</head>

<body>
    <div class="dashboard-container">
        <?php include '../includes/sidebar.php' ?>
        <main class="main-content">
            <div class="content">
                <!-- profile Section -->
                <div class="profile-container">
                    <div class="profile-card">
                        <img src="../assets/image/nctnvr.jpg" alt="Profile Picture" class="profile-pic">
                        <div class="profile-info">
                            <h2 class="full-name"><?php echo $admin_name; ?></h2>
                            <p class="role">
                                <i class="fa-solid fa-shield-halved"></i>
                                Admin
                            </p>
                            <div class="profile-stats">
                                <div class="stat-item">
                                    <span class="stat-value"><?php echo $total_students; ?></span>
                                    <span class="stat-label">Total Students</span>
                                </div>
                                <div class="stat-item">
                                    <span class="stat-value"><?php echo $pending_logs; ?></span>
                                    <span class="stat-label">Pending Approval</span>
                                </div>
                                <div class="stat-item">
                                    <span class="stat-value">3 Unread Messages</span>
                                    <span class="stat-label">Notifications</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- account Details Section -->
                <div class="account-section">
                    <h2 class="section-title">Account Details</h2>
                    <div class="account-container">
                        <div class="account-card">
                            <div class="account-header">
                                <i class="fa-regular fa-user"></i>
                                <h3>Personal Information</h3>
                            </div>
                            <div class="account-details">
                                <div class="detail-item">
                                    <span class="detail-label">Account Status</span>
                                    <span class="status-active">Active</span>
                                </div>
                                <div class="detail-item">
                                    <span class="detail-label">Username:</span>
                                    <span class="detail-value"><?php echo $admin_name; ?></span>
                                </div>
                                <div class="detail-item">
                                    <span class="detail-label">Email:</span>
                                    <span class="detail-value"><?php echo $admin_email; ?></span>
                                </div>
                                <div class="detail-item">
                                    <span class="detail-label">Phone Number:</span>
                                    <span class="detail-value">09012345678</span>
                                </div>
                            </div>
                            <div class="edit-button">
                                <button><i class="fa-solid fa-pen-to-square"></i> Edit Information</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</body>

</html>