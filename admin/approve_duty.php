<?php
session_start();
require_once '../config/database.php';

if (!isset($_SESSION['admin_id']) || !isset($_SESSION['admin_token'])) {
    header('Location: login.php');
    exit();
}

$error = "";
$success = "";

// Fetch pending duty logs from duty_logs.php using session token
$admin_token = $_SESSION['admin_token'];
$url = "http://localhost/duty-tracker/student/duty_logs.php";
$ch = curl_init($url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    "Authorization: Bearer $admin_token"
]);
curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
curl_setopt($ch, CURLOPT_VERBOSE, true);
$curl_output = fopen('php://temp', 'w+');
curl_setopt($ch, CURLOPT_STDERR, $curl_output);

$response = curl_exec($ch);
$http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$curl_error = curl_error($ch);
curl_close($ch);

// Debug cURL output
rewind($curl_output);
$verbose_log = stream_get_contents($curl_output);
fclose($curl_output);

if ($response === false) {
    $error = "cURL Error: Failed to connect to duty_logs.php | HTTP Code: $http_code | Error: $curl_error | Verbose: $verbose_log";
    $pending_logs = [];
} else {
    $data = json_decode($response, true);
    if (json_last_error() === JSON_ERROR_NONE && is_array($data)) {
        if ($data['success']) {
            $pending_logs = $data['logs'];
            if (empty($pending_logs)) {
                $error = "No pending duty logs found.";
            }
        } else {
            $pending_logs = [];
            $error = $data['message'] ?? "Failed to fetch duty logs.";
        }
    } else {
        $error = "Invalid JSON response: " . json_last_error_msg() . " | Raw Response: " . htmlspecialchars($response);
        $pending_logs = [];
    }
}

// Function to calculate hours worked with precision
function calculateHoursWorked($timeIn, $timeOut) {
    if (!$timeIn || !$timeOut) return 0;
    $time1 = new DateTime($timeIn);
    $time2 = new DateTime($timeOut);
    $interval = $time1->diff($time2);
    return round($interval->h + ($interval->i / 60), 2);
}

// Handle approval/rejection
if ($_SERVER["REQUEST_METHOD"] == "POST" && isset($_POST['log_id'], $_POST['action'])) {
    $log_id = $_POST['log_id'];
    $action = $_POST['action'];
    $admin_id = $_SESSION['admin_id'];

    $updated_date = isset($_POST['editdate']) ? $_POST['editdate'] : null;
    $updated_time_in = isset($_POST['timein']) ? $_POST['editdate'] . ' ' . $_POST['timein'] : null; // Combine date and time
    $updated_time_out = isset($_POST['timeout']) ? $_POST['editdate'] . ' ' . $_POST['timeout'] : null; // Combine date and time

    $pdo->beginTransaction();

    try {
        if ($action == 'Approved') {
            $stmt_log = $pdo->prepare("SELECT time_in, time_out, student_id FROM duty_logs WHERE id = ?");
            $stmt_log->execute([$log_id]);
            $log_data = $stmt_log->fetch(PDO::FETCH_ASSOC);

            if ($log_data) {
                $time_in = $updated_time_in ?: $log_data['time_in'];
                $time_out = $updated_time_out ?: $log_data['time_out'];
                $hours_worked = calculateHoursWorked($time_in, $time_out);

                $stmt_update = $pdo->prepare("
                    UPDATE duty_logs 
                    SET status = 'Approved', 
                        total_hours = ?, 
                        admin_id = ?, 
                        approved_at = NOW(),
                        time_in = ?,
                        time_out = ?
                    WHERE id = ?
                ");
                $stmt_update->execute([$hours_worked, $admin_id, $time_in, $time_out, $log_id]);

                $stmt_total_hours = $pdo->prepare("
                    SELECT IFNULL(SUM(total_hours), 0) 
                    FROM duty_logs 
                    WHERE student_id = ? AND status = 'Approved'
                ");
                $stmt_total_hours->execute([$log_data['student_id']]);
                $total_hours_rendered = $stmt_total_hours->fetchColumn();

                $stmt_student_update = $pdo->prepare("
                    UPDATE students 
                    SET total_hours = ? 
                    WHERE id = ?
                ");
                $stmt_student_update->execute([$total_hours_rendered, $log_data['student_id']]);
            }
        } else {
            $stmt_reject = $pdo->prepare("UPDATE duty_logs SET status = 'Rejected' WHERE id = ?");
            $stmt_reject->execute([$log_id]);
        }

        $pdo->commit();
        $success = "Duty log successfully updated.";
        header("Location: approve_duty.php");
        exit();
    } catch (Exception $e) {
        $pdo->rollBack();
        $error = "Error updating duty log: " . $e->getMessage();
    }
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
</head>
<body>
    <div class="dashboard-container">
        <?php include '../includes/sidebar.php'?>
        <main class="main-content">
            <header class="header-container">
                <div class="header-left">
                    <h2><i class="fas fa-users"></i> Approve Duty Logs</h2>
                </div>
                <div class="header-right">
                    <div class="search-sort-container">
                        <div class="search-container">
                            <i class="fas fa-search"></i>
                            <input type="text" id="searchInput" placeholder="Search...">
                        </div>
                        <div class="dropdown">
                            <img src="../assets/image/sort-icon.jpg" alt="Sort" onclick="toggleDropdown()">
                            <div class="dropdown-content" id="dropdown">
                                <select id="sortSelect">
                                    <option value="id">ID</option>
                                    <option value="student_id">Student ID</option>
                                    <option value="name">Name</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
            </header>

            <?php if (!empty($error)): ?>
                <p class="error"><?php echo $error; ?></p>
            <?php elseif (!empty($success)): ?>
                <p class="success"><?php echo $success; ?></p>
            <?php endif; ?>

            <section class="table-container">
                <div class="table-content">
                    <table id="studentsTable">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Student Name</th>
                                <th>Student ID</th>
                                <th>Duty Date</th>
                                <th>Time In</th>
                                <th>Time Out</th>
                                <th>Hours Worked</th>
                                <th>Total Hours Rendered</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php if (empty($pending_logs)): ?>
                                <tr><td colspan="10">No pending duty logs.</td></tr>
                            <?php else: ?>
                                <?php foreach ($pending_logs as $log): ?>
                                    <tr>
                                        <td><?php echo $log['id']; ?></td>
                                        <td><?php echo htmlspecialchars($log['student_name']); ?></td>
                                        <td><?php echo htmlspecialchars($log['student_id']); ?></td>
                                        <td><?php echo date('Y-m-d', strtotime($log['duty_date'])); ?></td>
                                        <td><?php echo date('h:i A', strtotime($log['time_in'])); ?></td>
                                        <td><?php echo $log['time_out'] ? date('h:i A', strtotime($log['time_out'])) : 'N/A'; ?></td>
                                        <td><?php echo number_format($log['hours_worked'], 2); ?> hrs</td>
                                        <td><?php echo number_format($log['total_hours'], 2); ?> hrs</td>
                                        <td class="<?php echo $log['status'] == 'Pending' ? 'status-pending' : ''; ?>">
                                            <?php echo htmlspecialchars($log['status']); ?>
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

    <div class="form-modal" id="form_popup">
        <div class="form-content">
            <h3>Review Time Log</h3>
            <form id="timeForm" method="POST" action="">
                <input type="hidden" id="log_id" name="log_id">
                <label for="edit_date">Date</label>
                <input type="date" id="edit_date" name="editdate" required>
                <label for="edit_timein">Time in</label>
                <input type="time" id="edit_timein" name="timein" required>
                <label for="edit_timeout">Time out</label>
                <input type="time" id="edit_timeout" name="timeout" required>
                <div class="buttons">
                    <button type="button" onclick="closeModal()">Cancel</button>
                    <button type="submit" name="action" value="Rejected" class="reject-button">Reject</button>
                    <button type="submit" name="action" value="Approved" class="approve-button">Approve</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function openModal(logData) {
            document.getElementById('form_popup').style.display = 'flex';
            document.getElementById('log_id').value = logData.id;
            document.getElementById('edit_date').value = logData.date;
            document.getElementById('edit_timein').value = logData.timeIn;
            document.getElementById('edit_timeout').value = logData.timeOut;
        }

        function closeModal() {
            document.getElementById('form_popup').style.display = 'none';
        }

        window.onclick = function(event) {
            var modal = document.getElementById('form_popup');
            if (event.target == modal) {
                closeModal();
            }
        }

        function toggleDropdown() {
            document.getElementById('dropdown').classList.toggle('show');
        }

        document.getElementById('searchInput').addEventListener('keyup', function() {
            const searchValue = this.value.toLowerCase();
            const table = document.getElementById('studentsTable');
            const rows = table.getElementsByTagName('tr');
            for (let i = 1; i < rows.length; i++) {
                let found = false;
                const cells = rows[i].getElementsByTagName('td');
                for (let j = 0; j < cells.length; j++) {
                    const cellText = cells[j].textContent.toLowerCase();
                    if (cellText.indexOf(searchValue) > -1) {
                        found = true;
                        break;
                    }
                }
                rows[i].style.display = found ? '' : 'none';
            }
        });
    </script>
</body>
</html>