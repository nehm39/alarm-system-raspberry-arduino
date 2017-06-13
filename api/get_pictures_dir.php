<?php 
include 'config.php'; 
if($_GET['user'] === $rLogin && $_GET['pass'] === $rPass)
{
print str_replace('./','',$rDir);
}
?>