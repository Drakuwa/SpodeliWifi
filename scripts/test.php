<?php

$command = $_POST['command'];
$content = $_POST['content'];
$content.="\n";

if($command=='get'){
	$collectionFile = "text";
	$fh = fopen($collectionFile, 'r');
	$theData = fread($fh, filesize($collectionFile));
	fclose($fh);

	$counterFile = "counter";
        $fh3 = fopen($counterFile, 'r');
        $version = fread($fh3, filesize($counterFile));
        fclose($fh3);

	if($version==$content){
		echo 'same version';
	}
	else {
		echo 'version: '.$version;
		echo $theData;
	}
}
elseif($command=='add'){
	$exists = 0;
	$collectionFile = "text";
	$fh = fopen($collectionFile, 'a') or die("can't open file");
	$fhTmp = fopen($collectionFile, "r") or die("can't open file for reading");

	if($fhTmp){
		while (($buffer = fgets($fhTmp)) !== false) {
		if($content==$buffer) $exists=1;
    		}
    		if (!feof($fhTmp)) {
        		echo "Error: unexpected fgets() fail\n";
    		}
		fclose($fhTmp);
	}

	if($exists){
		echo "AP already exists!\n";
	}
	else {
		fwrite($fh, $content);
		fclose($fh);

		$counterFile = "counter";
		$fh1 = fopen($counterFile, 'r');
		$count = fread($fh1, filesize($counterFile));
		fclose($fh1);

		$count = $count + 1;

		$fh2 = fopen($counterFile, 'w') or die("can't open file");
		fwrite($fh2, $count."\n");
		fclose($fh2);
		echo 'Success!';
	}
}
else{
	echo "Operation Failed!";
}

?>
