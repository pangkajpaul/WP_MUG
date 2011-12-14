<?php
// simulate responsing requested XML file

$data = array(
        array("name"=>"stop1", "x_pos"=>56.182391, "y_pos"=>15.600924, "distance"=>550),
        array("name"=>"stop2", "x_pos"=>56.166322, "y_pos"=>15.585523, "distance"=>3100),
        array("name"=>"stop3", "x_pos"=>56.186911, "y_pos"=>15.602303, "distance"=>600),
        );

$doc = new DOMDocument("1.0");
$node = $doc->createElement("markers");
$pernode = $doc->appendChild($node);
header("Content-type: text/xml");

foreach ($data as $row) {
        //echo$row['id']." ".$row['name']." ".$row['x_pos']." ".$row['y_pos']."<br />";
        $node = $doc->createElement("markers");
        $newnode = $pernode->appendChild($node);
        $newnode->setAttribute("name",$row['name']);
        $newnode->setAttribute("lat",$row['x_pos']);
        $newnode->setAttribute("long",$row['y_pos']);
        $newnode->setAttribute("distance",$row['distance']);
}

echo $doc->saveXML();
?>
