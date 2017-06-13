<?php
include 'config.php';
if ($_GET['user'] === $rLogin && $_GET['pass'] === $rPass) {
    $dir = $sDir . 'system.py';
    exec("sudo python $dir > /dev/null 2>/dev/null &");
    print 'success';
} else {
    http_response_code(403);
    print $forbiddenMsg;
}
?>