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

$alrutas = new stdClass();
$primero = true;
$sql = "SELECT id, ruta FROM rutas";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
  // output data of each row
  while($row = $result->fetch_assoc()) {
	  $alrutas->$row["id"] = $row["ruta"];	  
  }
} else {
  echo "0 results";
}
echo json_encode($alrutas);
$conn->close();
?>