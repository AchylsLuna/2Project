<?php
include 'config.php';

if (isset($_POST['signUp'])) {
    $username = $_POST['username'];
    $student_id = $_POST['student_id'];
    $password = $_POST['password'];
    $confirm_password = $_POST['confirm_password'];

    // if the passwords match
    if ($password != $confirm_password) {
        echo "Passwords do not match!";
    } else {
        //HASH PASSWORD
        $password = password_hash($password, PASSWORD_DEFAULT);

        // if the username or student_id already exists using prepared statements
        $checkUsername = $conn->prepare("SELECT * FROM users WHERE username = ? OR student_id = ?");
        $checkUsername->bind_param("ss", $username, $student_id);
        $checkUsername->execute();
        $result = $checkUsername->get_result();

        if ($result->num_rows > 0) {
            echo "Username or Student ID already exists!";
        } else {
            // Insert the new user into the database using prepared statements
            $insertQuery = $conn->prepare("INSERT INTO users(username, student_id, password) VALUES (?, ?, ?)");
            $insertQuery->bind_param("sss", $username, $student_id, $password);

            if ($insertQuery->execute()) {
                header("Location: index.php"); // Redirect to index page
            } else {
                echo "Error: " . $conn->error; // Show error if query fails
            }
        }
    }
}

if (isset($_POST['signIn'])) {
    // Get the username and password for login
    $username = $_POST['username'];
    $password = $_POST['password'];

    // Query to check if the username exists
    $sql = "SELECT * FROM users WHERE username = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();

        // Verify the password using password_verify()
        if (password_verify($password, $row['password'])) {
            session_start();
            $_SESSION['username'] = $row['username'];
            header("Location: homepage.php"); // Redirect to homepage
            exit();
        } else {
            echo "Incorrect password!";
        }
    } else {
        echo "Not Found, Incorrect Username or Password"; // Show error for incorrect login
    }
}
?>
