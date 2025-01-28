<?php
$hostName = "localhost";
$username = "root";
$password = "";
$dbName = "scholarly";


$conn = mysqli_connect($hostName, $username, $password, $dbName) or die ("Could not connect to database");

if (!$conn) {
    die("Something went wrong; " . mysqli_connect_error());
}
?>
