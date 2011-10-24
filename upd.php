<?php

$user = 'TRIAL-53698007';
$pass = 'xerbu3fhef';

$tmp_path = '/tmp/eset_upd';
$web_path = '/var/test/eset_upd';
$srv = 'http://um10.eset.com/eset_upd/v5/';

exec('wget http://um10.eset.com/eset_upd/v5/update.ver 2>&1', $output, $return);

if(!file_exists($tmp_path)){
  $path = explode('/', $tmp_path);
  if(!file_exists($path[0])){
    exec('mkdir ' . $path);
  }elseif (!file_exists($path[0])){
    exec('mkdir ' . $path[0] . '/' . $path[1]);
  }
};

$h = fopen($tmp_path . '/log', 'w+');
foreach ($output as $str){
  fwrite($h, $str . '\n');
}
fwrite($h, '\n' . $return);
$arr = parse_ini_file('update.ver4', TRUE, INI_SCANNER_RAW);




echo($arr['HOSTS']['Other']);

//var_dump(get_defined_vars());
?>

