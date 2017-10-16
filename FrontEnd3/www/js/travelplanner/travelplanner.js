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


function mappaCreaPercorso(id, mezzo, punti) {
    console.log("mappaCreaPercorso: "+id+","+mezzo+","+punti)
}

function mappaCreaStep(id, mezzo, punti) {
    console.log("mappaCreaStep: "+id+","+mezzo+","+punti)
}

function mappaCreaFermata(id, x, y, tipo, testo) {
    console.log("mappaCreaFermata: "+id+", "+x+", "+y+", "+tipo+", "+testo)
}

function mappaVisualizzaOggetto(id) {
    console.log("mappaVisualizzaOggetto: "+id)

}

function mappaNascondiOggetto(id) {
    console.log("mappaNascondiOggetto: "+id)
}

/**
 * Effettua una richiesta su travel planenr
 *
 * @param {string} tp_div the div where the result will be shown
 */

function  tpricerca(tp_div,from_lat,from_lng,to_lat,to_lng){


    var partenza = [];
    partenza.push({
        name:       "Punto da mappa",
        externalId: "partenza_input",
        x:          from_lng,
        y:          from_lat,
        tipo:       "coordinate"
    });
    var arrivo = [];
    arrivo.push({
        name:       "Punto da mappa",
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


    $.ajax({
        url:     'http://travelplanner.cup2000.it/ricerca/', //<--chiamata alle mappe
        type:    'POST',
        data:    json_oggetto,
        //async:   false, //<-- senza questo, non passa i dati fuori
        success: function(html){

            var lines = html.split("\n");
            for(var i =0; i<lines.length;i++)
                if(lines[i].trim().startsWith("map."))
                    lines[i] = "";
            var str = lines.join("\n") + "<div onclick='$(\"#"+tp_div+"\").hide()' class='ui-btn ui-btn-b ui-shadow ui-corner-all ui-icon-delete ui-btn-icon-right ui-btn-active ui-state-persist'>Chiudi</div>";
            $("#"+tp_div).html(str)
        }
    });
}