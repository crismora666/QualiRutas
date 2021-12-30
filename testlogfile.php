<?php
$target_dir = "uploads/";
$target_file = $target_dir . basename($_GET["log"]);
if (file_exists($target_file)) {
	if(filesize($target_file) == $_GET["logsize"]){
		echo "ExisteLog";
	} else{
		if (unlink($target_file)) {  
			echo "ListoParaSubir";  
		}  
		else {  
			echo "LogNoBorrado";  
		} 
	}	
} else {
	echo "ListoParaSubir";
}
?>