<?php
// Modified approve_duty.php
session_start();
require_once '../config/database.php';
require_once 'hours_format.php'; 

if (!isset($_SESSION['admin_id'])) {
    header('Location: login.php');
    exit();
}

$error = "";
$success = "";
$student_id_filter = isset($_GET['student_id']) ? $_GET['student_id'] : null;

// Base query with optional student filter
$query = "
    SELECT dl.id, dl.student_id, dl.duty_date, dl.time_in, dl.time_out, dl.status, dl.hours_worked,
           s.name AS student_name,
           (SELECT IFNULL(SUM(hours_worked), 0) 
            FROM duty_logs 
            WHERE student_id = dl.student_id 
            AND status = 'Approved') AS total_hours_rendered
    FROM duty_logs dl
    INNER JOIN students s ON dl.student_id = s.id
";
$params = [];

if ($student_id_filter) {
    $query .= " WHERE dl.student_id = ?";
    $params[] = $student_id_filter;
} else {
    $query .= " WHERE dl.status = 'Pending'";
}

$query .= " ORDER BY dl.duty_date DESC, dl.time_in ASC";

$stmt = $pdo->prepare($query);
$stmt->execute($params);
$duty_logs = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Handle approval/rejection (unchanged from your original)
if ($_SERVER["REQUEST_METHOD"] == "POST" && isset($_POST['log_id'], $_POST['action'])) {
    // ... [Keep your existing approval/rejection logic unchanged] ...
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Approve Duty Logs</title>
    <link rel="icon" href="../assets/image/icontitle.png" />
    <link rel="stylesheet" href="../assets/admin.css">
    <script src="../assets/search_filter.js"></script>
    <script src="../assets/delete_logs.js"></script>
    <script src="../assets/approve_logs.js"></script>
</head>
<body>
    <div class="dashboard-container">
        <?php include '../includes/sidebar.php'?>
        <main class="main-content">
            <header class="header-container">
                <div class="header-left">
                    <h2> <i class="fa-solid fa-hourglass-half" style="color: #f39c12"></i> 
                        <?php echo $student_id_filter ? "Duty Logs for Student $student_id_filter" : "Pending Duty Logs"; ?>
                    </h2>
                </div>
                <div class="header-right">
                    <div class="search-sort-container">
                        <div class="search-container">
                            <i class="fas fa-search"></i>
                            <input type="text" id="searchInput" placeholder="Search by Student ID">
                        </div>
                        <!-- Add filter form -->
                        <form method="GET" class="student-filter">
                            <input type="text" name="student_id" placeholder="Filter by Student ID" 
                                   value="<?php echo htmlspecialchars($student_id_filter ?? ''); ?>">
                            <button type="submit">Filter</button>
                            <?php if ($student_id_filter): ?>
                                <a href="approve_duty.php">Show All Pending</a>
                            <?php endif; ?>
                        </form>
                    </div>
                </div>
            </header>

            <?php if (!empty($error)): ?>
                <p class="error"><?php echo $error; ?></p>
            <?php elseif (!empty($success)): ?>
                <p class="success"><?php echo $success; ?></p>
            <?php endif; ?>

            <section class="table-container">
                <div class="table-actions">
                    <button class="delete-btn" id="deleteSelected">
                        <i class="fa fa-trash"></i> Delete Selected
                    </button>
                    <button class="approve-selected-btn" id="approveSelected">
                        <i class="fa fa-thumbs-up"></i> Approve Selected
                    </button>
                </div>

                <div class="table-content">
                    <table id="studentsTable">
                        <thead>
                            <tr>
                                <th><input type="checkbox" id="selectAll"></th>
                                <th>Student Name</th>
                                <th>Student ID</th>
                                <th>Duty Date</th>
                                <th>Time In</th>
                                <th>Time Out</th>
                                <th>Hours Worked</th>
                                <th>Total Hours</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php if (empty($duty_logs)): ?>
                                <tr>
                                    <td colspan="10">No duty logs found.</td>
                                </tr>
                            <?php else: ?>
                                <?php foreach ($duty_logs as $log): ?>
                                    <tr>
                                        <td><input type="checkbox" class="selectItem" 
                                            value="<?php echo htmlspecialchars($log['id']); ?>"></td>
                                        <td><?php echo htmlspecialchars($log['student_name']); ?></td>
                                        <td><?php echo htmlspecialchars($log['student_id']); ?></td>
                                        <td><?php echo date('Y-m-d', strtotime($log['duty_date'])); ?></td>
                                        <td><?php echo date('h:i A', strtotime($log['time_in'])); ?></td>
                                        <td><?php echo $log['time_out'] ? date('h:i A', strtotime($log['time_out'])) : 'N/A'; ?></td>
                                        <td><?php echo $log['hours_worked'] ? number_format($log['hours_worked'], 2) : 'N/A'; ?></td>
                                        <td><?php echo number_format($log['total_hours_rendered'], 2); ?></td>
                                        <td class="<?php echo strtolower($log['status']); ?>">
                                            <?php 
                                            if ($log['status'] == 'Pending') echo '<i class="fa-solid fa-clock"></i> ';
                                            echo htmlspecialchars($log['status']); 
                                            ?>
                                        </td>
                                        <td>
                                            <button type="button" onclick="openModal(<?php 
                                                echo htmlspecialchars(json_encode([
                                                    'id' => $log['id'],
                                                    'date' => date('Y-m-d', strtotime($log['duty_date'])),
                                                    'timeIn' => date('H:i', strtotime($log['time_in'])),
                                                    'timeOut' => $log['time_out'] ? date('H:i', strtotime($log['time_out'])) : '',
                                                    'student' => $log['student_name'],
                                                    'student_id' => $log['student_id']
                                                ])); 
                                            ?>)">
                                                <img src="../assets/image/threedots.svg" alt="actionbutton" class="three-dots">
                                            </button>
                                        </td>
                                    </tr>
                                <?php endforeach; ?>
                            <?php endif; ?>
                        </tbody>
                    </table>
                </div>
            </section>
        </main>
    </div>

    <!-- Keep your existing modal code -->
    <div class="form-modal" id="form_popup">
        <!-- ... [Your existing modal code remains unchanged] ... -->
    </div>

    <!-- Keep your existing scripts -->
    <script>
        // ... [Your existing JavaScript functions remain unchanged] ...
    </script>
</body>
</html>