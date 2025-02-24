<?php
session_start();

header("Content-Type: application/json");
include "connection.php";

$input = json_decode(file_get_contents("php://input"), true);

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    if (isset($input["email"]) && isset($input["password"]) && 
        !empty($input["email"]) && !empty($input["password"])) {

        $email = $input["email"];
        $password = $input["password"];

        // Check if the user exists
        $stmt = $conn->prepare("SELECT id, name, password FROM users WHERE email = ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $stmt->store_result();

        if ($stmt->num_rows > 0) {
            $stmt->bind_result($id, $name, $hashed_password);
            $stmt->fetch();

            // Verify password
            if (password_verify($password, $hashed_password)) {
                $_SESSION["user_id"] = $id;
                $_SESSION["user_name"] = $name;

                echo json_encode(["success" => true, "message" => "Login successful", "user" => ["id" => $id, "name" => $name]]);
            } else {
                echo json_encode(["success" => false, "message" => "Invalid password"]);
            }
        } else {
            echo json_encode(["success" => false, "message" => "User not found"]);
        }

        $stmt->close();
    } else {
        echo json_encode(["success" => false, "message" => "All fields are required"]);
    }

    $conn->close();
}
?>
