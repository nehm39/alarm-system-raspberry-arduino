<?php
include 'config.php';
if ($_GET['user'] === $rLogin && $_GET['pass'] === $rPass) {
    if (fopen($sDir . 'temp/stop', 'w')) {
        print 'success';
    }
} else {
    http_response_code(403);
    print $forbiddenMsg;
}
?>