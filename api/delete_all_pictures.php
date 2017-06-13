<?php
include 'config.php';
if ($_GET['user'] === $rLogin && $_GET['pass'] === $rPass) {
    $dir   = $rDir . '/*.jpg';
    $files = glob($dir);
    foreach ($files as $file) {
        if (is_file($file))
            unlink($file);
    }
    print 'success';
} else {
    http_response_code(403);
    print $forbiddenMsg;
}
?>