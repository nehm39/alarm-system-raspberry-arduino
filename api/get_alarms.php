<?php
include 'config.php';
if($_GET['user'] === $rLogin && $_GET['pass'] === $rPass)
{
	header('Content-Type: application/json');
$conn = mysql_connect($host, $user, $pass) or die ('Error');
mysql_select_db($dbname);
$result = mysql_query("SELECT * FROM alarms");
$rows = array();
while($r = mysql_fetch_assoc($result)) {
    $rows[] = $r;
}
print json_encode($rows);
mysql_close($conn);
}
else {
	http_response_code(403);
	print $forbiddenMsg;
}
?>