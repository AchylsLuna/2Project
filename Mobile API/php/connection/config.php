<?php
$hostName = "localhost";
$student_id = "root";
$email = "";
$password = "";
$dbName = "scholarly";

$conn = mysqli_connect($hostName, $student_id, $password, $dbName);

if (!$conn) {
    die("Connection failed: " . mysqli_connect_error());
}
?>
