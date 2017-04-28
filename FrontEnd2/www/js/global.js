
conf = {
    "dita_server_img" : "http://lume.morselli.unimore.it/DITA/img/",
    "dita_server" : "http://lume.morselli.unimore.it/DITA/WS/",
    "osm_tile" : "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
    "home_gg_marker" : "https://chart.googleapis.com/chart?chst=d_map_pin_icon&chld=home|0099ff",
    "pin_gg_marker" : "https://chart.googleapis.com/chart?chst=d_map_pin_letter",
    "localize" : true
};


var LOCALIZE_EVERY = 10000;
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
function POIS() {
    this.hotels = [];
    this.attractions  = [];
    this.monuments = [];
    this.museums = [];
    this.restaurants = [];
    this.parks = [];
    this.historical = [];
    this.religious = []
}


function Markers() {
    this.hotels  = new L.LayerGroup();
    this.attractions  = new L.LayerGroup();
    this.monuments = new L.LayerGroup();
    this.museums = new L.LayerGroup();
    this.restaurants = new L.LayerGroup();
    this.parks = new L.LayerGroup();
    this.historical = new L.LayerGroup();
    this.religious = new L.LayerGroup();
}


var markerIcons = {};
markerIcons["hotels"] = L.AwesomeMarkers.icon({
    //icon: 'glyphicon-cog',
    prefix: 'fa',
    icon: 'fa-bed',
    markerColor: 'orange'
});
markerIcons["attractions"] = L.AwesomeMarkers.icon({
    //icon: 'glyphicon-cog',
    prefix: 'fa',
    icon: 'fa-map-marker',
    markerColor: 'red'
});
markerIcons["monuments"] = L.AwesomeMarkers.icon({
    //icon: 'glyphicon-cog',
    prefix: 'fa',
    icon: 'fa-home',
    markerColor: 'darkred'
});
markerIcons["museums"] = L.AwesomeMarkers.icon({
    //icon: 'glyphicon-cog',
    prefix: 'fa',
    icon: 'fa-home',
    markerColor: 'blue'
});
markerIcons["restaurants"] = L.AwesomeMarkers.icon({
    //icon: 'glyphicon-cog',
    prefix: 'fa',
    icon: 'fa-cutlery',
    markerColor: 'darkgreen'
});
markerIcons["parks"] = L.AwesomeMarkers.icon({
    //icon: 'glyphicon-cog',
    prefix: 'fa',
    icon: 'fa-tree',
    markerColor: 'green'
});
markerIcons["historical"] = L.AwesomeMarkers.icon({
    //icon: 'glyphicon-cog',
    prefix: 'fa',
    icon: 'fa-history',
    markerColor: 'purple'
});
markerIcons["religious"] = L.AwesomeMarkers.icon({
    //icon: 'glyphicon-cog',
    prefix: 'fa',
    icon: 'fa-microchip',
    markerColor: 'darkpuple'
});

// Chiesa di San Francesco d'Assisi e pertinenze,from:MIBACT
// Duomo Di Modena, Via Lanfranco, Quartiere Cittanova, Centro Storico, Modena, MO, EMR, 41121, Italia
// Museo Storico dell'Accademia Militare,from:IBC

function format_name(name) {
    var name_array = name.split(',');
    var from = (name_array[1] && name_array[1].startsWith("from:")) ? name_array[1] : "from:nominatim";
    return name_array[0]+"<br/>"+from+"<br/>"
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