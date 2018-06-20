
confOLD = {
    "dita_server_files" : "http://lume.morselli.unimore.it/DITA/files/",
    "dita_server_img" : "http://lume.morselli.unimore.it/DITA/img/",
    "dita_server" : "http://lume.morselli.unimore.it/DITA/WS/",

    "osm_tile" : "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
    "home_gg_marker" : "https://chart.googleapis.com/chart?chst=d_map_pin_icon&chld=home|0099ff",
    "pin_gg_marker" : "https://chart.googleapis.com/chart?chst=d_map_pin_letter",
    "localize" : true
};


confNEW = {
    "dita_server_files" : "http://5.144.174.222:8080/DITA/files/",
    "dita_server_img" : "http://5.144.174.222:8080/DITA/img/",
    "dita_server" : "http://5.144.174.222:8080/DITA/WS/",

    "osm_tile" : "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
    "home_gg_marker" : "https://chart.googleapis.com/chart?chst=d_map_pin_icon&chld=home|0099ff",
    "pin_gg_marker" : "https://chart.googleapis.com/chart?chst=d_map_pin_letter",
    "localize" : true
};

conf = confNEW;


var isChrome = !!window.chrome && !!window.chrome.webstore;
if(isChrome) conf.localize = false;

/*
var platform = navigator.platform;
//console.log(navigator)
var isChrome = !!window.chrome && !!window.chrome.webstore;
//console.log(isChrome)
if(platform == "Win32" && isChrome == true)
conf.localize = false;
*/

$.postJSON = function(url, data, callback) {
    return jQuery.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        'type': 'POST',
        'url': url,
        'data': JSON.stringify(data),
        'dataType': 'json',
        'success': callback,
        'error':callback
    });
};


$.getJSON = function(url, callback) {
    return jQuery.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        'type': 'GET',
        'url': url,
        'success': callback,
        'error' : callback
    });
};


function getUrlParameter(sParam) {
    var sPageURL = decodeURIComponent(window.location.search.substring(1)),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;
    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : sParameterName[1];
        }
    }
}




// Chiesa di San Francesco d'Assisi e pertinenze,from:MIBACT
// Duomo Di Modena, Via Lanfranco, Quartiere Cittanova, Centro Storico, Modena, MO, EMR, 41121, Italia
// Museo Storico dell'Accademia Militare,from:IBC

function format_name(name) {
    var name_array = name.split(',');
    var from = (name_array[1] && name_array[1].startsWith("from:")) ? name_array[1] : "from: nominatim";
    return name_array[0];//+"<br/>"+from+"<br/>"
}

function format_name_from(name) {
    var name_array = name.split(',');
    var from = (name_array[1] && name_array[1].startsWith("from:")) ? name_array[1] : "from: nominatim";
    return from
}






$( document ).ready(function() {

    var city = window.sessionStorage.getItem("city");

    var menu = "<ul id='list_menu' class='jqm-list ui-alt-icon ui-nodisc-icon'>";
    menu += "<li data-filtertext='homepage' data-icon='home'><a href='index.html' target='_top' tkey='homepage'>Pagina principale</a></li>";
    if(city != null) menu += "<li data-filtertext='luoghi' data-icon = 'location'><a href='map.html' target='_top' data-icon='location' data-ajax='false'>Mappa</a></li>";
    if(city != null) menu += "<li data-filtertext='viaggi' data-icon = 'camera'><a href='checkuser.html' target='_top' data-icon='location' data-ajax='false' tkey='my_itineraries'>I miei viaggi</a></li>";
    if(city != null) menu += "<li data-filtertext='percorsi' data-icon = 'star'><a href='itineraries.html' target='_top' data-ajax='false' tkey='itineraries'>I percorsi più visti</a></li>";
    if(city != null) menu += "<li data-filtertext='luoghi' data-icon = 'location'><a href='map.html?crowd=true' target='_top' data-icon='location' data-ajax='false'>Affollamento</a></li>";
                     menu += "<li data-filtertext='account' data-icon = 'user'><a href='user.html' target='_top' data-ajax='false' tkey='user'>Utente</a></li>";
                     menu += "<li data-filtertext='assistenza' data-icon = 'phone'><a href='help.html' target='_top' data-ajax='false' tkey='help'>Assistenza</a></li>";
    menu += "</ul>";

    $("#list_menu").append(menu);
    $("#list_menu").trigger('pagecreate');
});





// loader code
$( document ).on( "click", ".show-page-loading-msg", function() {
    var $this = $( this ),
        theme = $this.jqmData( "theme" ) || $.mobile.loader.prototype.options.theme,
        msgText = $this.jqmData( "msgtext" ) || $.mobile.loader.prototype.options.text,
        textVisible = $this.jqmData( "textvisible" ) || $.mobile.loader.prototype.options.textVisible,
        textonly = !!$this.jqmData( "textonly" );
    html = $this.jqmData( "html" ) || "";
    $.mobile.loading( "show", {
        text: msgText,
        textVisible: textVisible,
        theme: theme,
        textonly: textonly,
        html: html
    });
}).on( "click", ".hide-page-loading-msg", function() {
    $.mobile.loading( "hide" );
});


/*********************************************************************************************************************/
/**********************                      TRANSLATE METHODS               *****************************************/

var langCode = window.sessionStorage.getItem("langCode");
if(langCode == null) langCode = navigator.language.substr (0, 2);

var dictionary = JSON.parse(window.sessionStorage.getItem("dictionary"));
if(!dictionary) {
    loadDictionary(langCode)
}

function loadDictionary(langCode) {
    return $.getJSON(conf.dita_server_files+'lang/' + langCode + '.json', function (jsdata) {
        dictionary = jsdata;
        translate();
        console.log(dictionary);
        window.sessionStorage.setItem("dictionary", JSON.stringify(dictionary));
    })
}


function translate() {
    if(dictionary)
        $("[tkey]").each(function (index) {
            var strTr = dictionary [$(this).attr('tkey')];
            $(this).html(strTr);
        });
}

function translateObjKeys(obj) {
    //console.log(dictionary);
    for(k in obj) {
        //console.log(k)
        var n = k.lastIndexOf(">");
        var prefix = k.substring(0,n+1);
        var name = k.substring(n+1).trim();
        //console.log(prefix)
        //console.log(name)
        if (name in dictionary) {
            //console.log("found "+k+" => "+jsdata[k])
            obj[prefix+dictionary[name]] = obj[k];
            delete obj[k]
        }
    }
}

function markerKey2Name(key) {
    if(key in dictionary) return  dictionary[key];
    else return key
}

function markerName2Key(name) {
    if(name in dictionary) return  dictionary[name];
    else return name
}


$(document).ready(function(){
    translate()
});


/*********************************************************************************************************************/
/**********************                      MAP METHODS                     *****************************************/




function getDistanceFromLatLonInM(lat1,lon1,lat2,lon2) {
    var R = 6371000; // Radius of the earth in m
    var dLat = deg2rad(lat2-lat1);  // deg2rad below
    var dLon = deg2rad(lon2-lon1);
    var a =
            Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
            Math.sin(dLon/2) * Math.sin(dLon/2)
        ;
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    var d = R * c; // Distance in m
    return d;
}

function deg2rad(deg) {
    return deg * (Math.PI/180)
}

var prevLat = Number(window.sessionStorage.getItem("prevLat"));
var prevLon = Number(window.sessionStorage.getItem("prevLon"));
if(prevLat == null) prevLat = 0;
if(prevLon == null) prevLon = 0;