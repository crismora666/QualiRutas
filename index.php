<?php
	//local: apikey & rutaspath
	set_time_limit(60);
	$rutaspath = "C:/rutas/";
	// $rutaspath = "G:/qrutas/rutas/";
	$rutaid = isset($_GET['qruta']) ? $_GET['qruta'] : '1';
	$driveid = isset($_GET['qdrive']) ? $_GET['qdrive'] : '0';
	
	if($driveid == '0'){	    
		$rutas_con = mysql_pconnect("localhost","root","enero2012");
		mysql_select_db("qrutas",$rutas_con);
		$rutas_query = "SELECT a.id FROM drives AS a 
						LEFT JOIN rutas AS b ON a.id_ruta=b.id 
						WHERE b.id=$rutaid
						ORDER BY a.fechaini DESC LIMIT 1;";
		$rutas_datos = mysql_query($rutas_query, $rutas_con);
		while($rutas_fila = mysql_fetch_assoc($rutas_datos)) {       	
			$driveid = $rutas_fila['id'];	  
		}	
		mysql_free_result($rutas_datos);
		mysql_close($rutas_con);
	}
	
	$repes_con = mysql_pconnect("localhost","root","enero2012");
	mysql_select_db("qrutas",$repes_con);
	$repes_query = "SELECT ruta FROM rutas WHERE id = $rutaid;";
	$repes_datos = mysql_query($repes_query, $repes_con);
	$i_rep = 0;
	while($repes_fila = mysql_fetch_assoc($repes_datos)) { 
		$rutatxt = $repes_fila['ruta'];
	}
	mysql_free_result($repes_datos);
	mysql_close($repes_con);
?>

<!DOCTYPE html>
<html>
	<head>
		<title>Qualirutas</title>
		<style>
			#section {
				position: fixed;
				left: 5vh;
				top: 10vh;
				width: 170px;
				max-height: 80vh;
				border: 3px solid #73AD21;
				background-color:#ffffff;
				z-index: 1; 
				overflow: auto; 
				font-size: small; 
				opacity: 0.5;
			}
			#section:hover {
				opacity: 1;
			}
			#snackbar {
			  visibility: hidden;
			  min-width: 250px;
			  margin-left: -125px;
			  background-color: #333;
			  color: #fff;
			  text-align: center;
			  border-radius: 2px;
			  padding: 16px;
			  position: fixed;
			  z-index: 1;
			  left: 50%;
			  bottom: 30px;
			  font-size: 17px;
			}

			#snackbar.show {
			  visibility: visible;
			  -webkit-animation: fadein 0.5s, fadeout 0.5s 2.5s;
			  animation: fadein 0.5s, fadeout 0.5s 2.5s;
			}

			@-webkit-keyframes fadein {
			  from {bottom: 0; opacity: 0;} 
			  to {bottom: 30px; opacity: 1;}
			}

			@keyframes fadein {
			  from {bottom: 0; opacity: 0;}
			  to {bottom: 30px; opacity: 1;}
			}

			@-webkit-keyframes fadeout {
			  from {bottom: 30px; opacity: 1;} 
			  to {bottom: 0; opacity: 0;}
			}

			@keyframes fadeout {
			  from {bottom: 30px; opacity: 1;}
			  to {bottom: 0; opacity: 0;}
			}			
		</style>
		<script>
			var qruta_map;
			var qruta_lat;
			var qruta_lon;
			var qruta_zoom;
			var qruta_coors = [];
			var event_selected = [false,false,false,false];
			var qruta_id = <?php print $rutaid . ";\n"; ?>
			var qdrive_id = <?php print $driveid . ";\n"; ?>
			var events;
			var map_events = [[],[],[],[]];

			function initGoogle(){
				initQrutas();
				var map_script = document.createElement("script");
				map_script.type = "text/javascript";
				//map_script.src = "https://maps.googleapis.com/maps/api/js?key=AIzaSyAIh586MQfcWDmENMVyEpahuQaimO-qcEE&callback=initialize";
				map_script.src = "https://maps.googleapis.com/maps/api/js?key=&callback=initialize";
				document.body.appendChild(map_script);  
			}
			
			function initQrutas(){
				<?php
					$repes_con = mysql_pconnect("localhost","root","enero2012");
					mysql_select_db("qrutas",$repes_con);
					$repes_query = "SELECT * FROM rutas WHERE id = $rutaid;";
					$repes_datos = mysql_query($repes_query, $repes_con);
					$i_rep = 0;
					while($repes_fila = mysql_fetch_assoc($repes_datos)) {  
						print "qruta_lat = " . $repes_fila['latitud'] . ";\n";
						print "                qruta_lon = " . $repes_fila['longitud'] . ";\n";
						print "                qruta_zoom = " . $repes_fila['zoom'] . ";";
					}
					mysql_free_result($repes_datos);
					mysql_close($repes_con);
				?> 
			}

			function initialize(){
				var mapProp = {
					center:new google.maps.LatLng(qruta_lat,qruta_lon),
					zoom:qruta_zoom,
					mapTypeId:google.maps.MapTypeId.ROADMAP
				};
				qruta_map = new google.maps.Map(document.getElementById("googleMap"),mapProp);  
				drawRuta();      
			}
			
			function drawRuta(){
				<?php      
					$repes_con = mysql_pconnect("localhost","root","enero2012");
					mysql_select_db("qrutas",$repes_con);
					print "qruta_coors[0] = [";
					$repes_query = "select a.latitud,a.longitud from rutaguis as a left join rutas as b on a.id_ruta=b.id where b.id=$rutaid order by a.id;";
					$repes_datos = mysql_query($repes_query, $repes_con);
					$i_rep = 0;
					while($repes_fila = mysql_fetch_assoc($repes_datos)) {
						if($i_rep > 0) print ",";        	
						print "{lat: " . $repes_fila['latitud'] . ", lng: " . $repes_fila['longitud'] . "}";
						$i_rep = $i_rep + 1;
					}
					print "];\n";  
					mysql_free_result($repes_datos);
					mysql_close($repes_con);      
				?>
				var qruta_linea = new google.maps.Polyline({
					path: qruta_coors[0],
					geodesic: true,
					strokeColor: '#0000FF',
					strokeOpacity: 1.0,
					strokeWeight: 2
				});
				qruta_linea.setMap(qruta_map);   
			}

			function putEvent(event_id){
				if(event_selected[event_id-1] == false){
					event_selected[event_id-1] = true;
					<?php
						print "events = {\"events\":[";
						$rutas_con = mysql_pconnect("localhost","root","enero2012");
						mysql_select_db("qrutas",$rutas_con);
						$rutas_query = "SELECT a.id_eventotipo,a.latitud,a.longitud,a.fecha FROM eventos AS a 
										LEFT JOIN drives AS b ON a.id_drive=b.id 
										WHERE b.id = $driveid;";
						$rutas_datos = mysql_query($rutas_query, $rutas_con);
						$i_rep = 0;
						while($rutas_fila = mysql_fetch_assoc($rutas_datos)) {
							if($i_rep > 0) print ",";        	
							print "{\"event\":" . $rutas_fila['id_eventotipo'] . ",\"lat\":" . $rutas_fila['latitud'] . ",\"lon\":" . $rutas_fila['longitud'] . ",\"fecha\":'" . $rutas_fila['fecha'] . "'}";
							$i_rep = $i_rep + 1;
						}
						mysql_free_result($rutas_datos);
						mysql_close($rutas_con);
						print "]};";
					?>   
					for (var i = 0; i < events.events.length; i++) {
						if (events.events[i].event == event_id) {
							var cmarker = new google.maps.Marker({
								position: {lat: events.events[i].lat, lng: events.events[i].lon},
								icon: event_id + '.png',
								animation: google.maps.Animation.DROP,
								title: events.events[i].fecha
							});
							map_events[event_id-1].push(cmarker);
							cmarker.setMap(qruta_map);  
						}
					}  
				} else {       
					event_selected[event_id-1] = false;
					for (var ww = 0; ww < map_events[event_id-1].length; ww++) {
						map_events[event_id-1][ww].setMap(null);
					}    
					map_events[event_id-1] = []; 
				}   
			}
			
			function CargaQrutas(){  
				location.href="index.php?qruta=" + document.getElementById("Qrutas").value;
			}
			
			function CargaQdrives(){  
				location.href="index.php?qruta=" + document.getElementById("Qrutas").value + "&qdrive=" + document.getElementById("Qdrives").value;
			}
			function enDesarrollo() {
			  var x = document.getElementById("snackbar");
			  x.className = "show";
			  setTimeout(function(){ x.className = x.className.replace("show", ""); }, 3000);
			}			

			window.onload = initGoogle;
		</script>
	</head>
	<body>
		<div id="section">
			<h2>Qualirutas 1.7</h2>
			<form action="index.php">
				<select id="Qrutas" name="rutas" onchange="CargaQrutas()">
					<?php
						$rutas_con = mysql_pconnect("localhost","root","enero2012");
						mysql_select_db("qrutas",$rutas_con);
						$rutas_query = "SELECT id,ruta FROM rutas ORDER BY id;";
						$rutas_datos = mysql_query($rutas_query, $rutas_con);
						$i_rep = 0;
						while($rutas_fila = mysql_fetch_assoc($rutas_datos)) {  	
							if($i_rep > 0) print "\n                    "; 
							print "<option value=\"" . $rutas_fila['id'] . "\"" . (($rutas_fila['id']==$rutaid)?" selected":"") . ">" . $rutas_fila['ruta'] . "</option>";
							$i_rep = $i_rep + 1;			
						}
						mysql_free_result($rutas_datos);
						mysql_close($rutas_con);
						print "\n";
					?>
				</select>
				</br>
				<p>Recorridos realizados: <?php      
					$repes_con = mysql_pconnect("localhost","root","enero2012");
					mysql_select_db("qrutas",$repes_con);
					$repes_query = "SELECT COUNT(*) AS n FROM drives 
									WHERE id_ruta = $rutaid;";
					$repes_datos = mysql_query($repes_query, $repes_con);
					while($repes_fila = mysql_fetch_assoc($repes_datos)) {           
						print $repes_fila['n'];
					}
					mysql_free_result($repes_datos);
					mysql_close($repes_con);
				?></p>
				<?php      
					$repes_con = mysql_pconnect("localhost","root","enero2012");
					mysql_select_db("qrutas",$repes_con);
					$repes_query = "SELECT llamadas,logfiles FROM drives 
									WHERE id=$driveid;";
					$repes_datos = mysql_query($repes_query, $repes_con);
					$total_calls = 0;
					$logfiles = 0;
					while($repes_fila = mysql_fetch_assoc($repes_datos)) { 
						$total_calls =  $repes_fila['llamadas'];
						$logfiles = $repes_fila['logfiles'];
						$arr_logfiles = explode(" ", $logfiles);
					}
					$repes_query = "SELECT a.id_eventotipo FROM eventos AS a 
									LEFT JOIN drives AS b ON a.id_drive=b.id 
									WHERE b.id = $driveid;";
					$repes_datos = mysql_query($repes_query, $repes_con);
					$call_drop = 0;
					$call_block = 0;
					$call_restablish = 0;
					$call_block_csfb = 0;
					while($repes_fila = mysql_fetch_assoc($repes_datos)) { 
						if($repes_fila['id_eventotipo'] == '1') $call_drop = $call_drop + 1;
						if($repes_fila['id_eventotipo'] == '2') $call_block = $call_block + 1;
						if($repes_fila['id_eventotipo'] == '3') $call_restablish = $call_restablish + 1;
						if($repes_fila['id_eventotipo'] == '4') $call_block_csfb = $call_block_csfb + 1;
					}
					mysql_free_result($repes_datos);
					mysql_close($repes_con);
				?></p>				
				<select id="Qdrives" name="drives" onchange="CargaQdrives()">
					<?php
						$rutas_con = mysql_pconnect("localhost","root","enero2012");
						mysql_select_db("qrutas",$rutas_con);
						$rutas_query = "SELECT id,fechaini FROM drives WHERE id_ruta = $rutaid 
										ORDER BY id DESC;";
						$rutas_datos = mysql_query($rutas_query, $rutas_con);
						$i_rep = 0;
						while($rutas_fila = mysql_fetch_assoc($rutas_datos)) {  	
							if($i_rep > 0) print "\n                    "; 
							print "<option value=\"" . $rutas_fila['id'] . "\"" . (($rutas_fila['id']==$driveid)?" selected":"") . ">" . $rutas_fila['fechaini'] . "</option>";
							$i_rep = $i_rep + 1;			
						}
						mysql_free_result($rutas_datos);
						mysql_close($rutas_con);
						print "\n";
					?>
				</select>
				</br>
				</br>
				<input type="checkbox" id="cd_id" name="cd_n" onclick="putEvent(1)">Call Dropped (<?php print $call_drop ?>)<br>
				<input type="checkbox" id="cb_id" name="cb_n" onclick="putEvent(2)">Call Blocked (<?php print $call_block ?>)<br>
				<input type="checkbox" id="cr_id" name="cr_n" onclick="putEvent(3)">Call Re-establish (<?php print $call_restablish ?>)<br>
				<input type="checkbox" id="cbcsfb_id" name="cbcsfb_n" onclick="putEvent(4)">Call Block CSFB (<?php print $call_block_csfb ?>)<br>
			</form> 
			</br>			
			<table>
				<tr>
					<td>Total Calls:</td>
					<td><?php print $total_calls ?></td>
				</tr>
				<tr>
					<td>Logfiles:</td>
					<td><?php 
						$logpath = $rutaspath . $rutatxt . '/logs_Finished/';
						$logfiles = array_diff(scandir($logpath), array('.', '..'));
						foreach ($arr_logfiles as $arr_logfile) {
							$found_logfile = false;
							foreach ($logfiles as $logfile) {	
								if(strpos($logfile,$arr_logfile)){
									print "<a href=\"https://" . $_SERVER['SERVER_NAME'] . "/qrutaslogs/$rutatxt/logs_Finished/$logfile\">$arr_logfile</a>\n                        ";
									$found_logfile = true;
								} 
							}
							if(!$found_logfile) print $arr_logfile . "\n                        ";
						}
					?></td>
				</tr>
			</table>
			</br>
			<button onclick="enDesarrollo()">Historico</button>
			<div id="snackbar">En desarrollo..</div>
		</div>
		<div id="googleMap" style="width:100%;height:95vh;"></div>
	</body>
</html>

