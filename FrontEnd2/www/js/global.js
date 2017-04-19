
conf = {
    "dita_server" : "http://lume.morselli.unimore.it/DITA/",
    "osm_tile" : "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
    "home_gg_marker" : "https://chart.googleapis.com/chart?chst=d_map_pin_icon&chld=home|0099ff",
    "pin_gg_marker" : "https://chart.googleapis.com/chart?chst=d_map_pin_letter",
    "localize" : false
};




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


var getUrlParameter = function getUrlParameter(sParam) {
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
};


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


