
conf = {
    "dita_server_files" : "http://lume.morselli.unimore.it/DITA/files/",
    "dita_server_img" : "http://lume.morselli.unimore.it/DITA/img/",
    "dita_server" : "http://lume.morselli.unimore.it/DITA/WS/",

    "osm_tile" : "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
    "home_gg_marker" : "https://chart.googleapis.com/chart?chst=d_map_pin_icon&chld=home|0099ff",
    "pin_gg_marker" : "https://chart.googleapis.com/chart?chst=d_map_pin_letter",
    "localize" : true
};

var platform = navigator.platform;

if(platform == "Win32")
    conf.localize = false;




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
        'success': callback
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
    var from = (name_array[1] && name_array[1].startsWith("from:")) ? name_array[1] : "from:nominatim";
    return name_array[0];//+"<br/>"+from+"<br/>"
}

function format_name_from(name) {
    var name_array = name.split(',');
    var from = (name_array[1] && name_array[1].startsWith("from:")) ? name_array[1] : "from:nominatim";
    return from
}





var mIcons = JSON.parse(window.sessionStorage.getItem("mIcons"));
console.log(mIcons);
if(!mIcons) {
    $.getJSON(conf.dita_server_files + 'markerIcons.json', function (jsdata) {
        mIcons = jsdata;
        window.sessionStorage.setItem("mIcons", JSON.stringify(mIcons));
        console.log(mIcons)
    })
}







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

var LOCALIZE_EVERY = 1000;
var REROUTE_EVERY = 20000;
var SEND_POSITION_EVERY_METERS = 20;
var MAX_POIS_IN_MAP = 10;


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

var dragged = false;
var centerMarker = null;
var prevLat = 0;
var prevLon = 0;
var start;


function localize(position) {
    if(getDistanceFromLatLonInM(position.coords.latitude,position.coords.longitude,prevLat,prevLon) > SEND_POSITION_EVERY_METERS) {
        $.getJSON(conf.dita_server + 'localize?lat=' + position.coords.latitude + "&lon=" + position.coords.longitude + "&user=" + JSON.parse(window.localStorage.getItem("user")).email, function (data, status) {
        });
        console.log("localized at " + position.coords.latitude + "," + position.coords.longitude);
        prevLat = position.coords.latitude;
        prevLon = position.coords.longitude;
        start = prevLat+","+prevLon;
    }
    if(!dragged)
        mymap.panTo([position.coords.latitude, position.coords.longitude]);
    if (centerMarker == null) {

        var icon = L.divIcon({
            type: 'div',
            className: 'marker',
            html: "<span class=\"fa-col-blue\"><i class=\"fa fa-dot-circle-o fa-3x fa-rotate-dyn\"></i></span>"
        });
        centerMarker = L.marker([position.coords.latitude, position.coords.longitude], {icon: icon}).addTo(mymap);
        mymap.setZoom(15)
    }


    centerMarker.setLatLng([position.coords.latitude, position.coords.longitude]);
    selectMarkers(pois,mymap.getBounds());

    window.setTimeout(function () {
        navigator.geolocation.getCurrentPosition(localize)
    }, LOCALIZE_EVERY)
}