<?php
$hostName = "localhost";
$dbUser = "root";
$dbPassword = "";
$dbName = "scholarly";

$conn = mysqli_connect($hostName, $dbUser, $dbPassword, $dbName);

// Debug connection
if (!$conn) {
    die("Connection failed: " . mysqli_connect_error());
} else {
    echo "Database Connected Successfully!";  // Temporary message for debugging
}
