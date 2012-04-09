<?php
	require('mysql.config.inc');
	$mysqli = new mysqli($host, $username, $password, $db); 		

	// select all album information from one album 		
	$singleAquery = "SELECT * FROM Albums WHERE aid=" + $_GET['aid'];
	$singleAresult = $mysqli->query($singleAquery);

	// Get all of the pictures in the album			
	$Pquery = "SELECT url,caption FROM Pictures NATURAL JOIN InAlbum WHERE aid="
		+ $_GET['aid'];
	$Presult = $mysqli->query($Pquery);			
	$numPics = $Presult->num_rows;

	$singleAarray = $singleAresult->fetch_assoc(); 
	echo "<div id= profileBox><h1>" .$singleAarray['title']. "</h1><br />";
	echo "<div class= imageBkgrdBox><div class= imageBox><img src = '"; 
	if ($singleAarray['cover_pic'] != "") { 
		echo $singleAarray['cover_pic']. "' /></div></div><br />"; 
	} else {
		echo "noImg.jpg' /></div></div><br />"; 
	}
	echo $singleAarray['caption']. "<br /><br />"; 
	echo "Created: " .$singleAarray['date_created']. "<br />Last Modified: " 
		.$singleAarray['date_modified']. "<br />" .$numPics. " pictures in album </div>"; 

	echo "<div id= \"picturesBox\">";
	while ($arrayP = $Presult->fetch_row()) {  
		echo "<img src = '";
		echo $arrayP[0];
		echo "' class= mediumPic alt='album picture here' onhover='return false;' 
		title='" .$arrayP[1]. "'/>"; 
	} 
	echo '</div>';	
	
	$mysqli->close();
?>