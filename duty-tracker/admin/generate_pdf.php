<?php
require_once('../vendor/tecnickcom/tcpdf/tcpdf.php');
require_once '../config/database.php';

// Debugging: Check if student_id is being passed correctly
if (!isset($_GET['student_id']) || empty($_GET['student_id'])) {
    die("Error: Student ID not provided.");
}

$student_id = $_GET['student_id'];
$status_filter = $_GET['status'] ?? 'all';

// Fetch student details
$stmt = $pdo->prepare("SELECT student_id, name, course, department, year_level, hk_duty_status FROM students WHERE student_id = ?");
$stmt->execute([$student_id]);
$student = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$student) {
    die("Error: Student not found.");
}

// Assign student details
$student_id_display = $student['student_id'];
$student_name = $student['name'];
$student_course = $student['course'];
$student_department = $student['department'];
$student_year = $student['year_level'];
$student_hk_status = $student['hk_duty_status'];

// Fetch duty logs using correct student_id mapping
$query = "SELECT * FROM duty_logs WHERE student_id = (SELECT id FROM students WHERE student_id = ?)";
$params = [$student_id];

if ($status_filter !== 'all' && in_array($status_filter, ['Approved', 'Rejected'])) {
    $query .= " AND status = ?";
    $params[] = $status_filter;
} else {
    $query .= " AND status IN ('Approved', 'Rejected')";
}

$query .= " ORDER BY duty_date DESC, time_in DESC";
$stmt = $pdo->prepare($query);
$stmt->execute($params);
$duty_logs = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Check if there are logs
if (empty($duty_logs)) {
    die("No duty logs found for this student.");
}

// Create PDF
$pdf = new TCPDF('L', 'mm', 'A4', true, 'UTF-8', false);
$pdf->SetCreator('Admin Panel');
$pdf->SetAuthor('Admin');
$pdf->SetTitle("Student Duty Logs - $student_name");
$pdf->SetSubject('Duty Log Report');
$pdf->setPrintHeader(false);
$pdf->setPrintFooter(false);
$pdf->AddPage();

// Set fonts
$pdf->SetFont('dejavusans', 'B', 16); // Title Font

// Center title
$pdf->Cell(0, 10, "STUDENTS DUTY LOG REPORT", 0, 1, 'C');

// Add space for margins
$pdf->Ln(5);

// Student details
$pdf->SetFont('dejavusans', '', 12);
$html = "<p><strong>Student Name:</strong> $student_name</p>
          <p><strong>Student ID:</strong> $student_id_display</p>
         <p><strong>Course:</strong> $student_course</p>
         <p><strong>Department:</strong> $student_department</p>
         <p><strong>Year Level:</strong> $student_year</p>
         <p><strong>HK Duty Status:</strong> $student_hk_status</p>";

$pdf->writeHTML($html, true, false, true, false, '');
$pdf->Ln(5); // Add spacing

// Table headers
$pdf->SetFont('dejavusans', 'B', 12);
$html = "<table border='1' cellpadding='5' cellspacing='0' style='border-collapse:collapse; width: 100%; text-align:center;'>
            <thead>
                <tr style='background-color:#f2f2f2;'>
                    <th><strong>Duty Date</strong></th>
                    <th><strong>Time In</strong></th>
                    <th><strong>Time Out</strong></th>
                    <th><strong>Hours Worked</strong></th>
                    <th><strong>Status</strong></th>
                </tr>
            </thead>
            <tbody>";

// Reset font for table data
$pdf->SetFont('dejavusans', '', 11);
$total_hours = 0;

foreach ($duty_logs as $log) {
    $time_in = date('h:i A', strtotime($log['time_in']));
    $time_out = !empty($log['time_out']) ? date('h:i A', strtotime($log['time_out'])) : 'N/A';
    
    if ($log['status'] === 'Rejected') {
        $hours_worked = "0 hrs";
    } else {
        if (!empty($log['time_out'])) {
            $hours_worked_seconds = strtotime($log['time_out']) - strtotime($log['time_in']);
            $hours_worked = round($hours_worked_seconds / 3600, 2); // Get hours as float
            $full_hours = floor($hours_worked);
            $minutes = round(($hours_worked - $full_hours) * 60);
            $hours_worked = $full_hours . " hr" . ($full_hours > 1 ? "s" : "");
            if ($minutes > 0) {
                $hours_worked .= " {$minutes} min";
            }
        } else {
            $hours_worked = "N/A";
        }
    }
    
    if ($log['status'] === 'Approved' && !empty($log['time_out'])) {
        $total_hours_seconds = strtotime($log['time_out']) - strtotime($log['time_in']);
        $total_hours += $total_hours_seconds / 3600; // Total hours in decimal format
    }
    
    $status = htmlspecialchars($log['status']);

    $html .= "<tr>
                <td>{$log['duty_date']}</td>
                <td>$time_in</td>
                <td>$time_out</td>
                <td>$hours_worked</td>
                <td>$status</td>
              </tr>";
}

$html .= "</tbody></table>";

// Format total hours worked in hours and minutes
$total_seconds = $total_hours * 3600; // Convert total hours to seconds
$hours = floor($total_seconds / 3600); // Get full hours
$minutes = round(($total_seconds % 3600) / 60); // Get remaining minutes

$total_hours_formatted = "";
if ($hours > 0) {
    if ($minutes > 0) {
        $total_hours_formatted = number_format($hours, 0) . " hr" . ($hours > 1 ? "s" : "") . " {$minutes} min";
    } else {
        $total_hours_formatted = number_format($hours, 0) . " hr" . ($hours > 1 ? "s" : "");
    }
} else {
    $total_hours_formatted = $minutes > 0 ? "{$minutes} min" : "0 min";
}

$html .= "<p><strong>Total Hours Worked: </strong> $total_hours_formatted</p>";

// Write HTML to PDF
$pdf->writeHTML($html, true, false, true, false, '');

// Add space for verification
$pdf->Ln(42);

// Verification section (bottom-right)
$pdf->SetFont('dejavusans', 'B', 12);
$pdf->Cell(0, 10, "Verified by: ____________________", 0, 1, 'R');
$pdf->SetFont('dejavusans', '', 12);
$pdf->Cell(0, 10, "Expert Teacher/Dean", 0, 1, 'R');

// Output PDF
$pdf->Output("Duty_Logs_$student_name.pdf", 'D');
?>