<?php
$target_dir = "uploads/";
$target_file = $target_dir . basename($_GET["log"]);
$renamed_file = basename($_GET["rutaid"]."_".$_GET["sysname"]."_".$_GET["log"]);

// Checking If File Already Exists 
if(file_exists($renamed_file)) 
{ 
	echo "Error While Renaming $target_file" ; 
} 
else
{ 
	if(rename( $target_file, $renamed_file)) 
	// if(rename( $target_file, "../../../qrutas/ftp/$renamed_file"))
		{ 
			echo "LognameRenamed" ; 
		} 
		else
		{ 
			echo "A File With The Same Name Already Exists" ; 
		} 
} 
?>