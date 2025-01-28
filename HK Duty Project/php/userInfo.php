<?php 
include 'connect.php';

if(isset($_POST['signUp'])){
    $username = $_POST['username'];
    $password = $_POST['password'];
    $student_id = $_POST['student_id'];
    $password = md5($password); // Hashing the password

    // Check if the student_id already exists
    $checkStudentId = "SELECT * FROM users WHERE student_id='$student_id'";
    $result = $conn->query($checkStudentId);
    if($result->num_rows > 0){
        echo "Student ID Already Exists!";
    }
    else{
        // Insert new user into the database
        $insertQuery = "INSERT INTO users(username, password, student_id)
                        VALUES ('$username', '$password', '$student_id')";
        if($conn->query($insertQuery) == TRUE){
            header("location: index.php"); // Redirect to the homepage or login page
        }
        else{
            echo "Error: " . $conn->error;
        }
    }
}

if(isset($_POST['signIn'])){
    $student_id = $_POST['student_id'];
    $password = $_POST['password'];
    $password = md5($password); // Hashing the password for comparison

    // Check if the student_id and password match
    $sql = "SELECT * FROM users WHERE student_id='$student_id' AND password='$password'";
    $result = $conn->query($sql);
    if($result->num_rows > 0){
        session_start();
        $row = $result->fetch_assoc();
        $_SESSION['student_id'] = $row['student_id']; // Store student_id in the session
        header("Location: homepage.php"); // Redirect to the homepage
        exit();
    }
    else{
        echo "Not Found, Incorrect Student ID or Password";
    }
}
?>