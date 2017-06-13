<?php
include 'config.php';
if($_GET['user'] === $rLogin && $_GET['pass'] === $rPass)
{
$rows   = array();
foreach(glob($rDir.'*.jpg') as $file) {
	$rows[] = str_replace($rDir, "", $file);
}
print json_encode(array_reverse($rows));
} else {
    http_response_code(403);
    print $forbiddenMsg;
}
?>