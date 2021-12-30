<?php
$servername = "localhost";
$username = "root";
$password = "enero2012";
$dbname = "qrutas";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
// Check connection
if ($conn->connect_error) {
  die("Connection failed: " . $conn->connect_error);
}

$sql = "INSERT INTO qrutas.logsxsubir (log, id_ruta) VALUES ('".$_GET["log"]."',".$_GET["rutaid"].")";

if ($conn->query($sql) === TRUE) {
  echo "LognameInserted";
} else {
  echo "Error: " . $sql . "<br>" . $conn->error;
}

$conn->close();
?>