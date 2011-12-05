<?php

$user = '';
$pass = '';

$tmp_path = '/home/valery1707/nod32/mirror_4_file/';
$web_path = '/home/valery1707/nod32/mirror_4_web/';


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

function load_ini_file($file_path) {
  $ver_a = explode('.', phpversion());
  $ver = implode('', array($ver_a[0], $ver_a[1]));

  logg("Load ini from '$file_path' with php version $ver");
  if($ver < 53){
    $settings_str = file_get_contents($file_path);
    $settings = parse_ini_string($settings_str, true);
  }else {
    $settings = parse_ini_file($file_path, TRUE, INI_SCANNER_RAW);
  }
  //var_dump($settings);
  return $settings;
}


logg("Downloading update.ver");
download($full_tail . 'update.ver', $tmp_path . 'arc/');
if ($official) {
  rename($tmp_path . 'arc/' . 'update.ver', $tmp_path . 'arc/' . 'update.rar');
  exec($unrar . ' x -o+ ' . $tmp_path . 'arc/' . 'update.rar ' . $tmp_path . 'arc/', $out, $ret);
  //var_dump($out, 'Out');
  //var_dump($ret, 'Ret');
  if($ret != 0) err('Error unpacking update.ver');  
}

$settings = load_ini_file($tmp_path . 'arc/' . 'update.ver');
//var_dump($settings); exit();
$version_new = $settings['ENGINE2']['versionid'];

$settings_current = array();
$version_current = 0;
if(is_file($web_path . 'update.ver')) {
  $settings_current = load_ini_file($web_path . 'update.ver');
  $version_current = $settings_current['ENGINE2']['versionid'];
}

logg("Versions: new ($version_new) vs old ($version_current)");
if ($version_new > $version_current) {
  $settings_new = array();
  $filescount_total = 0;
  $filescount_download = 0;
  $filescount_keeped = 0;
  foreach ($settings as $name => $section) {
    //var_dump($section);
    if (isset($section['file'])) {
      if (isset($section['language']) && $section['language'] != $ru) continue;

      $filescount_total++;
      $file_url = $section['file'];
      $file_name = basename($file_url);
      $filesize_ini = $section['size'];
      $filesize_old = 0;
      $filesize_url = url_size($file_url);
      if (is_file($web_path . $file_name)) {
        $filesize_old = filesize($web_path . $file_name);
      }

      if ($filesize_url != $filesize_old) {
        logg("Going to download '$file_name' on size diff (old: $filesize_old, url: $filesize_url, ini: $filesize_ini)");
        download($file_url, $tmp_path);
        $filescount_download++;
        $filesize_new = filesize($tmp_path . $file_name);
      } else {
        logg("Keep old '$file_name' on file size equal (old: $filesize_old, url: $filesize_url, ini: $filesize_ini)");
        $filescount_keeped++;
        $filesize_new = $filesize_old;
      }

      if ($filesize_new != $filesize_url) {
        err("Checksum error in file '$file_name': real($filesize_new) vs url($filesize_url)");
      }

      //var_dump($name);exit;
      $section['file'] =  $file_name;
      $settings_new[$name] = $section;
    }
  }//foreach
  //var_dump($settings_new);
  logg("Processing new settings done (total: $filescount_total; $download: $filescount_download; keeped: $filescount_keeped)!");

  logg("Creating new update.ver");
  $settings_file = '';
  foreach ($settings_new as $name => $section) {
    $settings_file .= '[' . $name . "]\n";
    foreach ($section as $key => $value ) {
      $settings_file .= $key . '=' . $value . "\n";
    }
  }
  $resource = fopen($tmp_path . 'update.ver', 'w');
  //echo "Resource for update.ver $resource\n";
  $ret = fwrite($resource, $settings_file);
  if ($ret === false) {
    err("Cannot create new update.ver!!!");
  }
  //var_dump($settings_file);
  logg("New settings writed!");

/*
  logg("Clear $web_path");
  chkpath($web_path);
  $resource = opendir($web_path);
  echo "Resource for $web_path $resource\n";
  while (false !== ($file = readdir($resource))) {
    if($file != '.' && $file != '..') unlink($web_path . $file);
  }
*/

  logg("Copy new files from $tmp_path to $web_path");
  chkpath($web_path);
  chkpath($tmp_path);
  $resource = opendir($tmp_path);
  while (false !== ($file_name = readdir($resource))) {
    if ($file_name != '.' && $file_name != '..' && $file_name != 'arc' && $file_name != 'log') {
      $file_name_web = $web_path . $file_name;
      $file_name_tmp = $tmp_path . $file_name;
      logg("Copy $file_name");
      if (is_file($file_name_web)) {
        unlink($file_name_web);
      }
      rename($file_name_tmp, $file_name_web);
    }
  }
  echo "Done!\n";
}//if ($version_new > $version_current)


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
  //logg('<--' . $file);
  //var_dump($proto . $user . ':' . $pass . '@' . $srv . '/' . $file);
  exec('wget ' . $proto . $user . ':' . $pass . '@' . $srv . '/' . $file .' -O ' . $dir. '/' . basename($file) . ' -a '.  $dir . '/dl.log', $out, $ret);
  //var_dump($out);
  //var_dump($ret);
  //exit();
}

function url_size($file) {
  global $proto, $user, $pass, $srv;
  //http://php.net/manual/ru/function.get-headers.php
  stream_context_set_default(
    array(
        'http' => array(
            'method' => 'HEAD'
        )
    )
  );
  $headers = get_headers($proto . $user . ':' . $pass . '@' . $srv . '/' . $file, 1);
  //var_dump($headers);exit();
  return intval($headers['Content-Length']);
}

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
  //if(is_file($tmp_path . 'error.log')) unlink($tmp_path . 'error.log');
  error_log($message . ". File: " . __FILE__ . ' on line: ' . __LINE__ . ' on ' . date('Y-m-d H:i:s') . '\n', 3, $tmp_path . 'error.log'); 
  logg("ERR: " . $message);
}

function logg($msg) {
  global $tmp_path;
  echo $msg . "\n";
  error_log($msg . "\n", 3, $tmp_path . 'log/' . date('Y_m_d_h_i') . '.log');
}

//echo($arr['HOSTS']['Other']);

//var_dump(get_defined_vars());
?>
