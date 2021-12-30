<?php
//print_r($_FILES);

$target_dir = "uploads/";
$target_file = $target_dir . basename($_FILES["upfile"]["name"]);
$uploadOk = 1;
$target_extension = strtolower(pathinfo($target_file,PATHINFO_EXTENSION));

// Check if image file is a actual image or fake image
//if(isset($_POST["submit"])) {
//  $check = getimagesize($_FILES["upfile"]["tmp_name"]);
//  if($check !== false) {
//    echo "File is an image - " . $check["mime"] . ".";
//    $uploadOk = 1;
//  } else {
//    echo "File is not an image.";
//    $uploadOk = 0;
//  }
//}

// Check if file already exists
if (file_exists($target_file)) {
  echo "Sorry, file already exists.";
  $uploadOk = 0;
}

// Check file size
//if ($_FILES["upfile"]["size"] > 500000) {
//  echo "Sorry, your file is too large.";
//  $uploadOk = 0;
//}

// Allow certain file formats
//if($target_extension != "jpg" && $target_extension != "png" && $target_extension != "jpeg"
//&& $target_extension != "gif" ) {
//  echo "Sorry, only JPG, JPEG, PNG & GIF files are allowed.";
//  $uploadOk = 0;
//}

// Check if $uploadOk is set to 0 by an error
if ($uploadOk == 0) {
  echo "Sorry, your file was not uploaded.";
// if everything is ok, try to upload file
} else {
  if (move_uploaded_file($_FILES["upfile"]["tmp_name"], $target_file)) {
    echo "LogSubido";
  } else {
    echo "Sorry, there was an error uploading your file.";
  }
}
?>