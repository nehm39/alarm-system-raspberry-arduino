<?php
include 'config.php';
if($_GET['user'] === $rLogin && $_GET['pass'] === $rPass && !empty($_GET['file']))
{
    if (file_exists($rDir . $_GET['file'])) {
        if (unlink($rDir . $_GET['file']))
		{
		print 'success';
		}
		else
		{
		print 'error deleting';
		}
    }
	else
	{
	print 'file not found';
	}
}
?>