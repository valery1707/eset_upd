<?php

$user = 'TRIAL-53698007';
$pass = 'xerbu3fhef';

$tmp_path = '/tmp/test/eset_upd/tmp/';
$web_path = '/tmp/test/eset_upd/www/';
$srv = 'http://um10.eset.com/eset_upd/v5/';
$srv = 'update.eset.com';
$tail = 'eset_upd';
$db = '5';




$full_tail = '/' . $tail . '/v' . $db;
$proto = 'http://';
#exec('wget http://um10.eset.com/eset_upd/v5/update.ver 2>&1', $output, $return);
function download($file, $dir){
global $proto, $user, $pass, $srv, $full_tail; 
  exec('wget ' . $proto .$user . ':' . $pass . '@' . $srv . $full_tail . '/' . $file .' -O ' . $dir. '/' . $file . ' -a '.  $dir . '/dl.log');
}

download('update.ver', $tmp_path);


//var_dump($host);
function chkpath($path){
 $out = array();
 $ret = array();
 if(!file_exists($path)) exec('mkdir --parents ' . $path, $out, $ret);
//var_dump($out, 'Out');
//var_dump($ret, 'Ret');
//$ret === 0 ? return 'OK' : return 'Error'
}

/**
 *  Провверка существования пути
 *
//chkpath($tmp_path);



/*
$h = fopen($tmp_path . '/log', 'w+');
foreach ($output as $str){
  fwrite($h, $str . '\n');
}
fwrite($h, '\n' . $return);

$arr = parse_ini_file('update.ver4', TRUE, INI_SCANNER_RAW);
*/



//echo($arr['HOSTS']['Other']);

//var_dump(get_defined_vars());
?>

