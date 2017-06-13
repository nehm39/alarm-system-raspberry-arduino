<?php
include 'config.php';
if($_GET['user'] === $rLogin && $_GET['pass'] === $rPass) {
	print $successMsg;
}
else {
	http_response_code(403);
	print $forbiddenMsg;
}

?>