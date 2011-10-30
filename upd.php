<?php

$user = 'TRIAL-53698007';
$pass = 'xerbu3fhef';

$tmp_path = '/tmp/test/eset_upd/tmp/';
$web_path = '/tmp/test/eset_upd/www/';
$srv = 'http://um10.eset.com/eset_upd/v5/';
$srv = 'update.eset.com';
$tail = 'eset_upd';
$db = '4';

$unrar = '/usr/bin/unrar';
$ru = 1049;
$en = 1033;



$full_tail = '/' . $tail . '/v' . $db . '/';
$proto = 'http://';
#exec('wget http://um10.eset.com/eset_upd/v5/update.ver 2>&1', $output, $return);



download($full_tail . 'update.ver', $tmp_path . 'arc/');
rename($tmp_path . 'arc/' . 'update.ver', $tmp_path . 'arc/' . 'update.rar');
exec($unrar . ' x -o+ ' . $tmp_path . 'arc/' . 'update.rar ' . $tmp_path . 'arc/', $out, $ret);
//var_dump($out, 'Out');
//var_dump($ret, 'Ret');
if($ret != 0) err('Error unpacking update.ver'); 
$settings = parse_ini_file($tmp_path . 'arc/' . 'update.ver', TRUE, INI_SCANNER_RAW);
//var_dump($settings); exit();
$settings_new = array();
foreach ($settings as $name => $section){
	//var_dump($section);
	if(isset($section['file'])){
		if (isset($section['language']) && $section['language'] != $ru) continue;
		//echo 'Going to download ' .$section['file'];
		download($section['file'], $tmp_path);
		if( filesize($tmp_path . basename($section['file'])) != $section['size']) err("Checksum error in file: " . basename($section['file']));
		//var_dump($name);exit;
		$settings_new[$name] = $section;
	}
}
//var_dump($settings_new);
//echo "New settings done!\n";
$settings_file = '';
foreach ($settings_new as $name => $section) {
	$settings_file .= '[' . $name . "]\n\n";
	foreach ($section as $key => $value ) {
		$settings_file .= $key . '=' . $value . "\n";
	}
	$settings_file .= "\n";
}

$resource = fopen($tmp_path . 'update.ver', 'w');
$ret = fwrite($resource, $settings_file);
if($ret === false) err("Cannot create new update.ver!!!");
//var_dump($settings_file);
//echo "New settings writed!\n";
$version_new = $settings['ENGINE2']['versionid'];

$settings_current = array();
$version_current = 0;
if(is_file($web_path . 'update.ver')) {
	$settings_current = parse_ini_file($web_path . 'update.ver', TRUE, INI_SCANNER_RAW);
	$version_current = $settings_current['ENGINE2']['versionid'];
}

if($version_new >= $version_current){
	$resource = opendir($web_path);
	while (false !== ($file = readdir($resource))) {
       if($file != '.' || $file != '..') unlink($web_path . $file);
    }
    
    $resource = opendir($tmp_path);
    while (false !== ($file = readdir($resource))) {
    	if($file != '.' || $file != '..' || $file != 'arc') rename($tmp_path . $file, $web_path . $file);
    }
    //echo "Done!\n";
}


//var_dump($host);
function chkpath($path){
 $out = array();
 $ret = array();
 if(!file_exists($path)) exec('mkdir --parents ' . $path, $out, $ret);
//var_dump($out, 'Out');
//var_dump($ret, 'Ret');
//$ret === 0 ? return 'OK' : return 'Error'
}


function download($file, $dir){
	global $proto, $user, $pass, $srv;
	chkpath($dir);
	//var_dump($proto . $user . ':' . $pass . '@' . $srv . '/' . $file);
	exec('wget ' . $proto . $user . ':' . $pass . '@' . $srv . '/' . $file .' -O ' . $dir. '/' . basename($file) . ' -a '.  $dir . '/dl.log', $out, $ret);
	//var_dump($out);
	//var_dump($ret);
	//exit();
	
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



function err($message){
	global $tmp_path;
	if(is_file($tmp_path . 'error.log')) unlink($tmp_path . 'error.log');
	error_log($message . ". File: " . __FILE__ . ' on line: ' . __LINE__ . ' on ' . date('d/m/Y H:i:s'), 3, $tmp_path . 'error.log'); 
}




//echo($arr['HOSTS']['Other']);

//var_dump(get_defined_vars());
?>

