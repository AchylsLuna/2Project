<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Scholarship Type</title>
</head>
<body>
    <form method="POST" action="process_type.php">
        <p>Scholarship Type</p>
        <select name="scholarship_type">
            <option value="25%">25%</option>
            <option value="50%">50%</option>
            <option value="75%">75%</option>
        </select>
        <p>Duty Status</p>
        <select name="duty_status">
            <option value="Active">Active</option>
            <option value="Inactive">Inactive</option>
        </select>
        <p>Schedule</p>
        <select name="schedule">
            <option value="Morning">Morning</option>
            <option value="Afternoon">Afternoon</option>
            <option value="Evening">Evening</option>
        </select>
        <p>Expert Teacher</p>
        <select name="expert_teacher">
            <option value="Josh Dacasin">Josh Dacasin</option>
            <option value="Reynard Visperas">Reynard Visperas</option>
            <option value="Veronica Canlas">Veronica Canlas</option>
        </select>
        <p>Subject Code</p>
        <select name="subject_code">
            <option value="ITE300">ITE300</option>
            <option value="ITE393">ITE393</option>
            <option value="ITE400">ITE400</option>
        </select>
        <br>
        <input type="submit" value="Submit">
    </form>
</body>
</html>
