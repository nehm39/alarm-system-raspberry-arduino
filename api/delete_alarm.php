<?php
include 'config.php';
if ($_GET['user'] === $rLogin && $_GET['pass'] === $rPass && !empty($_GET['id'])) {
    $conn = mysql_connect($host, $user, $pass) or die('Error');
    mysql_select_db($dbname);
    $id = $_GET['id'];
    if (mysql_query("DELETE FROM alarms WHERE id = $id")) {
        print 'success';
    }
    mysql_close($conn);
} else {
    http_response_code(403);
    print $forbiddenMsg;
}
?>