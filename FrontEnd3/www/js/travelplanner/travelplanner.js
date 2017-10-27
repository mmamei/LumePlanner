/**
 * Created by marco on 16/10/2017.
 */


/*
 REQUIRES:

<script src="js/travelplanner/risultati.js"></script>
<script src="js/travelplanner/travelplanner.js"></script>
<link rel="stylesheet" href="css/bootstrap.min.css">
<link rel="stylesheet" href="css/travelplanner/main.css">
<link rel="stylesheet" href="css/travelplanner/table.css">
*/



function convert2geojson(punti) {
    var coords = [];
    for(var i=0; i<punti.length;i++)
        coords.push([punti[i].x,punti[i].y])
    console.log(coords);
    var geoj = {
        type: "LineString",
        coordinates: coords
    };
    return geoj
}

var iti_type; // clicked or itinerary

var tp_coords2Clicked = null;
var tp_coords2Itinerary = null;
var tp_markers = {};

function mappaCreaPercorso(id, mezzo, punti) {
    if(iti_type == ROUTE_TYPE.CLICKED) {
        tp_coords2Clicked = convert2geojson(punti);
        tp_markers[id] = L.geoJSON(tp_coords2Clicked, {style: pathStyle2Clicked}).bindPopup("percorso,"+id+","+mezzo)
    }
    if(iti_type == ROUTE_TYPE.ITINERARY) {
        tp_coords2Itinerary = convert2geojson(punti);
        tp_markers[id] = L.geoJSON(tp_coords2Itinerary, {style: pathStyle2Itinerary}).bindPopup("percorso,"+id+","+mezzo)
    }
    console.log("--------------------------------------------------------------");
    console.log("mappaCreaPercorso: "+id+","+mezzo+","+punti)
}

function mappaCreaStep(id, mezzo, punti) {
    //tp_markers[id] = L.geoJSON(convert2geojson(punti), {style: pathStyle2Itinerary}).bindPopup("step,"+id+","+mezzo);
    //console.log("mappaCreaStep: "+id+","+mezzo+","+punti)
}

function mappaCreaFermata(id, lng, lat, tipo, testo) {
    if(tipo=="Partenza" || tipo == "Arrivo") return;
    console.log("mappaCreaFermata: "+id+", "+lng+", "+lat+", "+tipo+", "+testo);
    tp_markers[id] = L.marker([lat,lng], {icon: tpIcons[tipo]}).bindPopup("<b>"+id+": "+tipo+"</b><br>"+testo);
}

function mappaVisualizzaOggetto(id) {
    if(tp_markers[id]) tp_markers[id].addTo(mymap)
}

function mappaNascondiOggetto(id) {
    if(tp_markers[id]) tp_markers[id].remove()
}

/**
 * Effettua una richiesta su travel planenr
 *
 * @param {string} tp_div the div where the result will be shown
 */



function  tpricerca(tp_div,from_name,from_lat,from_lng,to_name,to_lat,to_lng,type){

    iti_type = type;
    var partenza = [];
    partenza.push({
        name:       from_name,
        externalId: "partenza_input",
        x:          from_lng,
        y:          from_lat,
        tipo:       "coordinate"
    });
    var arrivo = [];
    arrivo.push({
        name:       to_name,
        externalId: "arrivo_input",
        x:          to_lng,
        y:          to_lat,
        tipo:       "coordinate"
    });

    var d = new Date();

    var data           = d.getDate()+"/"+(1+d.getMonth())+"/"+d.getFullYear();
    var ora            = d.getHours()+":"+d.getMinutes();

    //creo l'oggetto per l'invio
    var oggetto_transito = {
        partenza:      partenza,
        arrivo:        arrivo,
        data:          data,
        ora:           ora,
        scelta_orario: "partenza",
        mezzi_usati:   ["AU", "AS", "AE", "R", "IC", "ES"],
        transito:      ""
    };

    var json_oggetto = 'percorso=' + JSON.stringify(oggetto_transito);

    console.log($.support.cors);
    $.ajax({
        url:     'http://travelplanner.cup2000.it/ricerca/', //<--chiamata alle mappe
        type:    'POST',
        data:    json_oggetto,
        success: function(html){
            var lines = html.split("\n");
            for(var i =0; i<lines.length;i++) {
                if (lines[i].trim().startsWith("map."))
                    lines[i] = "";

                if(lines[i].indexOf("<img src=\"\" class") != -1) {
                    var classS = lines[i].indexOf("<img src=\"\" class=\"")+"<img src=\"\" class=\"".length;
                    var classE = lines[i].indexOf("\"",classS);
                    var mezzo =  lines[i].substring(classS,classE);
                    console.log("********* "+mezzo);
                    lines[i] = lines[i].replace("<img src=\"\"","<img src=\"img/travelplanner/mezzi/"+mezzo+".png\"")
                }

                if(lines[i].indexOf("<img src=\"#\" class") != -1) {
                    var classS = lines[i].indexOf("<img src=\"#\" class=\"")+"<img src=\"#\" class=\"".length;
                    var classE = lines[i].indexOf("\"",classS);
                    var mezzo =  lines[i].substring(classS,classE);
                    console.log("********* "+mezzo);
                    lines[i] = lines[i].replace("<img src=\"#\"","<img src=\"img/travelplanner/mezzi/"+mezzo+".png\"")
                }

                lines[i] = lines[i].replace(new RegExp("onmouseover", 'g'), "onclick")
            }

            var str = lines.join("\n") + "<div onclick='$(\"#"+tp_div+"\").hide()' class='ui-btn ui-btn-b ui-shadow ui-corner-all ui-icon-delete ui-btn-icon-right ui-btn-active ui-state-persist'>Chiudi</div>";
            $("#"+tp_div).html(str)
        },
        error: function(data,textStatus,error) {
            var str = "Error<br>";
            str += JSON.stringify(data)+"<br>";
            str += textStatus+"<br>";
            str += error+"<br>";
            str += "<div onclick='$(\"#"+tp_div+"\").hide()' class='ui-btn ui-btn-b ui-shadow ui-corner-all ui-icon-delete ui-btn-icon-right ui-btn-active ui-state-persist'>Chiudi</div>";
            $("#"+tp_div).html(str)
        }
    });
}