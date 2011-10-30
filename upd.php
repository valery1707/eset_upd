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



//download($full_tail . 'update.ver', $tmp_path);
//rename($tmp_path . 'update.ver', $tmp_path . 'update.rar');
exec($unrar . ' x -o+ ' . $tmp_path . 'update.rar ' . $tmp_path , $out, $ret);
//var_dump($out, 'Out');
//var_dump($ret, 'Ret');
$settings = parse_ini_file($tmp_path . 'update.ver', TRUE, INI_SCANNER_RAW);
//var_dump($settings); exit();
$settings_new = array();
foreach ($settings as $section){
	//var_dump($section);
	if(isset($section['file'])){
		if (isset($section['language']) && $section['language'] != $ru) continue;
		//echo 'Going to download ' .$section['file'];
		download($section['file'], $tmp_path);
		if( filesize($tmp_path . basename($section['file'])) != $section['size']) unlink($tmp_path . basename($section['file']));
		$settings_new[] = $section; 
	}
}
//var_dump($settings);



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



//echo($arr['HOSTS']['Other']);

//var_dump(get_defined_vars());
?>

