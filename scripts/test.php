<?php

$command = $_POST['command'];
$content = $_POST['content'];
$content.="\n";

if($command=='get'){
	$myFile = "text";
	$fh = fopen($myFile, 'r');
	$theData = fread($fh, filesize($myFile));
	fclose($fh);

	$myFile2 = "counter";
        $fh3 = fopen($myFile2, 'r');
        $theData2 = fread($fh3, filesize($myFile2));
        fclose($fh3);

	if($theData2==$content){
		echo 'same version';
	}
	else {
		echo 'version: '.$theData2;
		echo $theData;
	}
}
elseif($command=='add'){
	$myFile = "text";
	$fh = fopen($myFile, 'a') or die("can't open file");
	fwrite($fh, $content);
	fclose($fh);

	$counterFile = "counter";
	$fh1 = fopen($counterFile, 'r');
	$count = fread($fh1, filesize($counterFile));
	$count = $count + 1;

	$fh2 = fopen($counterFile, 'w') or die("can't open file");
	fwrite($fh2, $count."\n");
	fclose($fh1);
	fclose($fh2);
	echo 'Success!';
}
else{
	echo "Operation Failed!";
}

?>
