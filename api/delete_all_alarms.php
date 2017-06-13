<?php
include 'config.php';
if ($_GET['user'] === $rLogin && $_GET['pass'] === $rPass) {
    $conn = mysql_connect($host, $user, $pass) or die('Error');
    mysql_select_db($dbname);
    if (mysql_query("DELETE FROM alarms")) {
        print 'success';
    }
    mysql_close($conn);
} else {
    http_response_code(403);
    print $forbiddenMsg;
}
?>