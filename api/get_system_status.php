<?php
include 'config.php';
if($_GET['user'] === $rLogin && $_GET['pass'] === $rPass)
{
$dir = $sDir . 'temp/running';
if (file_exists($dir))
{
	print 'working';
}
else {
	print 'not working';
}
}
else {
	http_response_code(403);
	print $forbiddenMsg;
}
?>