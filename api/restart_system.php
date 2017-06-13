<?php
include 'config.php';
if ($_GET['user'] === $rLogin && $_GET['pass'] === $rPass) {
    $stop = $sDir . 'temp/stop';
    fopen($stop, 'w');
    sleep(3);
    
    $running = $sDir . 'temp/running';
    if (file_exists($running)) {
        unlink($running);
    }
    unlink($stop);
    
    $dir = $sDir . 'system.py';
    exec("sudo python $dir > /dev/null 2>/dev/null &");
    print 'success';
} else {
    http_response_code(403);
    print $forbiddenMsg;
}
?>