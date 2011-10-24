<?php

$user = 'TRIAL-53698007';
$pass = 'xerbu3fhef';

$tmp_path = '/tmp/eset_upd';
$web_path = '/var/test/eset_upd';
$srv = 'http://um10.eset.com/eset_upd/v5/';

//exec('wget http://um10.eset.com/eset_upd/v5/update.ver 2>&1', $output, $return);

$arr = parse_ini_file('update.ver4', TRUE, INI_SCANNER_RAW);




echo($arr['HOSTS']['Other']);

//var_dump(get_defined_vars());
?>

