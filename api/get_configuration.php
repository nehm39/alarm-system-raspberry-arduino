<?php
include 'config.php';
if ($_GET['user'] === $rLogin && $_GET['pass'] === $rPass && !empty($_GET['name'])) {
    $conn = mysql_connect($host, $user, $pass) or die('Error');
    mysql_select_db($dbname);
    $name   = $_GET['name'];
    $result = mysql_query("SELECT value FROM configuration WHERE name = '$name' limit 1");
    if ($row = mysql_fetch_assoc($result)) {
        print $row['value'];
    }
    mysql_close($conn);
} else {
    http_response_code(403);
    print $forbiddenMsg;
}
?>