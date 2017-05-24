
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


var LOCALIZE_EVERY = 1000;
var REROUTE_EVERY = 20000;


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
        'success': callback
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


var mIcons = JSON.parse(window.sessionStorage.getItem("mIcons"));
console.log(mIcons);
if(!mIcons) {
    $.getJSON(conf.dita_server_files + 'markerIcons.json', function (jsdata) {
        mIcons = jsdata;
        window.sessionStorage.setItem("mIcons", JSON.stringify(mIcons));
        console.log(mIcons)
    })
}

var langCode = navigator.language.substr (0, 2);
var dictionary = JSON.parse(window.sessionStorage.getItem("dictionary"));
if(!dictionary) {
    loadDictionary()
}

function loadDictionary() {
    $.getJSON(conf.dita_server_files+'lang/' + langCode + '.json', function (jsdata) {
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
    console.log(dictionary);
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


$(document).ready(function(){
    translate()
});




