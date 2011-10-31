<?php

$user = 'TRIAL-53698007';
$pass = 'xerbu3fhef';

$tmp_path = '/home/u3436/tmp/eset_upd/';
$web_path = '/home/u3436/domains/roskoclub.org.ua/eset_upd/';
$srv = 'http://um10.eset.com/eset_upd/v5/';
$srv = 'update.eset.com';
$tail = 'eset_upd';
$db = '4';

$unrar = '/usr/bin/unrar';
$ru = 1049;
$en = 1033;
$official = true;


$full_tail = '/' . $tail . '/v' . $db . '/';
$proto = 'http://';
#exec('wget http://um10.eset.com/eset_upd/v5/update.ver 2>&1', $output, $return);


if(!function_exists('parse_ini_string')){
    function parse_ini_string($str, $ProcessSections=false){
        $lines  = explode("\n", $str);
        $return = Array();
        $inSect = false;
        foreach($lines as $line){
            $line = trim($line);
            if(!$line || $line[0] == "#" || $line[0] == ";")
                continue;
            if($line[0] == "[" && $endIdx = strpos($line, "]")){
                $inSect = substr($line, 1, $endIdx-1);
                continue;
            }
            if(!strpos($line, '=')) // (We don't use "=== false" because value 0 is not valid as well)
                continue;
           
            $tmp = explode("=", $line, 2);
            if($ProcessSections && $inSect)
                $return[$inSect][trim($tmp[0])] = ltrim($tmp[1]);
            else
                $return[trim($tmp[0])] = ltrim($tmp[1]);
        }
        return $return;
    }
}


//echo "Downloading update.ver\n";
download($full_tail . 'update.ver', $tmp_path . 'arc/');
if($official){
  rename($tmp_path . 'arc/' . 'update.ver', $tmp_path . 'arc/' . 'update.rar');
  exec($unrar . ' x -o+ ' . $tmp_path . 'arc/' . 'update.rar ' . $tmp_path . 'arc/', $out, $ret);
  //var_dump($out, 'Out');
  //var_dump($ret, 'Ret');
  if($ret != 0) err('Error unpacking update.ver');  
}

$ver_a = explode('.', phpversion());
$ver = implode('', array($ver_a[0], $ver_a[1]));
if($ver < 53){
  $settings_str = file_get_contents($tmp_path . 'arc/' . 'update.ver');
  $settings = parse_ini_string($settings_str, true);
}else $settings = parse_ini_file($tmp_path . 'arc/' . 'update.ver', TRUE, INI_SCANNER_RAW);
//var_dump($settings); exit();


$version_new = $settings['ENGINE2']['versionid'];
$settings_current = array();
$version_current = 0;
if(is_file($web_path . 'update.ver')) {
  if($ver < 53){
    $settings_str = file_get_contents($web_path . 'update.ver');
    $settings = parse_ini_string($settings_str, true);
  }else $settings_current = parse_ini_file($web_path . 'update.ver', TRUE, INI_SCANNER_RAW);

	$version_current = $settings_current['ENGINE2']['versionid'];
}


if($version_new > $version_current){
  
  $settings_new = array();
  foreach ($settings as $name => $section){
	//var_dump($section);
	if(isset($section['file'])){
		if (isset($section['language']) && $section['language'] != $ru) continue;
		//echo 'Going to download ' .$section['file'];
		download($section['file'], $tmp_path);
		if( filesize($tmp_path . basename($section['file'])) != $section['size']) err("Checksum error in file: " . basename($section['file']));
		//var_dump($name);exit;
                $section['file'] =  basename($section['file']);
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

//echo "Creating new update.ver\n";
$resource = fopen($tmp_path . 'update.ver', 'w');
//echo "Resource for update.ver $resource\n";
$ret = fwrite($resource, $settings_file);
if($ret === false) err("Cannot create new update.ver!!!");
//var_dump($settings_file);
//echo "New settings writed!\n";

    chkpath($web_path);
	$resource = opendir($web_path);
	//echo "Resource for $web_path $resource\n";
	while (false !== ($file = readdir($resource))) {
       if($file != '.' && $file != '..') unlink($web_path . $file);
    }
    
    chkpath($web_path);
    $resource = opendir($tmp_path);
    while (false !== ($file = readdir($resource))) {
    	if($file != '.' && $file != '..' && $file != 'arc') rename($tmp_path . $file, $web_path . $file);
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

