

var mymap;
var pois = JSON.parse(window.sessionStorage.getItem("pois"));
var mIcons = JSON.parse(window.sessionStorage.getItem("mIcons"));
console.log(mIcons);
var markerIcons = {};
for(k in mIcons) {
    var x = mIcons[k].split(",");
    markerIcons[k] = L.AwesomeMarkers.icon({
        prefix: x[0],
        icon: x[1],
        markerColor: x[2]
    });
}




startM = {
    lat : 0,
    lng: 0,
    icon: L.divIcon({
        type: 'div',
        className: 'marker',
        html: "<span class=\"fa-col-blue fa-stack fa-lg\"><i class=\"fa fa-home fa-stack-2x\"></i></span>"
    })
};

endM = {
    lat : 0,
    lng: 0,
    icon: L.divIcon({
        type: 'div',
        className: 'marker',
        html: "<span class=\"fa-col-green fa-stack fa-lg\"><i class=\"fa fa-flag-checkered fa-stack-2x\"></i></span>"
    })
};


var LOCALIZE_EVERY = 1000;
var REROUTE_EVERY = 20000;
var SEND_POSITION_EVERY_METERS = 20;
var MAX_POIS_IN_MAP = 10;

var dragged = false;
var centerMarker = null;


var cityLonLatBbox = JSON.parse(window.sessionStorage.getItem("citybbox"));
var out_city_alert_fired = false;

function localize(position) {

    var lat = position.coords.latitude;
    var lng = position.coords.longitude;

    if(getDistanceFromLatLonInM(lat,lng,prevLat,prevLon) > SEND_POSITION_EVERY_METERS) {
        $.getJSON(conf.dita_server + 'localize?lat=' + lat + "&lon=" + lng + "&user=" + JSON.parse(window.localStorage.getItem("user")).email, function (data, status) {
        });
        console.log("localized at " + lat + "," + lng);
        prevLat = lat;
        prevLon = lng;
        window.sessionStorage.setItem("prevLat",prevLat);
        window.sessionStorage.setItem("prevLon",prevLon);
    }

    if (centerMarker == null) {
        var icon = L.divIcon({
            type: 'div',
            className: 'marker',
            html: "<span class=\"fa-col-blue\"><i class=\"fa fa-dot-circle-o fa-3x fa-rotate-dyn\"></i></span>"
        });
        centerMarker = L.marker([lat, lng], {icon: icon}).addTo(mymap);
        mymap.setZoom(15)
    }
    centerMarker.setLatLng([lat, lng]);

    var inCity = cityLonLatBbox[0] <= lng && lng <= cityLonLatBbox[2] &&
                 cityLonLatBbox[1] <= lat && lat <= cityLonLatBbox[3];
    console.log(cityLonLatBbox);
    console.log("localized at " + lat + "," + lng);
    console.log(inCity);

    if(!next_step && !inCity && !out_city_alert_fired) {
        out_city_alert_fired = true;
        alert("Sei ancora troppo lontano dalla città per posizionarti sulla mappa")
    }

    if((next_step && !dragged) || (!next_step && !dragged && inCity))
        mymap.panTo([lat, lng]);

    if(!next_step && !dragged && !inCity)
        mymap.fitBounds([
            [cityLonLatBbox[1], cityLonLatBbox[0]],
            [cityLonLatBbox[3], cityLonLatBbox[2]]
        ]);



    if(!next_step)
        selectMarkers(pois,mymap.getBounds());
    else {
        drawStartEndPlacemarks();
        computeRoute()
    }



    window.setTimeout(function () {
        navigator.geolocation.getCurrentPosition(localize)
    }, LOCALIZE_EVERY)
}




var minLat = 1000;
var minLon = 1000;
var maxLat = -1000;
var maxLon = -1000;

var markers = new L.LayerGroup();
function selectMarkers(pois,bbox) {

    markers.clearLayers();
    markers = new L.LayerGroup();

    var visiblePois = [];

    for(var type in pois) {
        //console.log(type);
        if (pois[type])
            for (var i = 0; i < pois[type].length; i++) {

                var x = pois[type][i];
                var lat = x.geometry.coordinates[1];
                var lon = x.geometry.coordinates[0];
                if (bbox) {
                    var ll = bbox.getSouthWest();
                    var tr = bbox.getNorthEast();
                    if (ll.lat < lat && lat < tr.lat &&
                        ll.lng < lon && lon < tr.lng)
                        visiblePois.push(x)
                }
                else visiblePois.push(x)
            }
    }

    visiblePois.sort(function(a,b) {
        if(a.importance < b.importance) return 1;
        if(a.importance > b.importance) return -1;
        return 0
    });


    for(var i=0; i<Math.min(MAX_POIS_IN_MAP,visiblePois.length);i++) {
        var x = visiblePois[i];
        var type = x.category;
        var lat = x.geometry.coordinates[1];
        var lon = x.geometry.coordinates[0];
        var id = x.place_id;

        var info = "<span class='popcontent'>"+format_name(x.display_name)+"<br></span>" +
            "<a  target=\"_top\" href=\"visit.html?type="+type+"&id="+id+"\">Visit</a><br>" +
            "<span style='color:lightsteelblue'>"+format_name_from(x.display_name)+":"+x.type+"</span><br>"+
            "<span onclick='$(\"#popup\").hide()' class='ui-corner-all ui-icon-delete ui-btn-icon-right ui-btn-active ui-state-persist'></span>";



        var icon = markerIcons[type];
        if(!icon) icon = markerIcons["attractions"];


        var marker = L.marker([lat, lon], {icon: icon, mypopup:info});
        //marker.bindPopup("<h2>"+format_name(x.display_name)+"</h2><br><span style='color:lightsteelblue'>"+format_name_from(x.display_name)+":"+x.place_id+"</span><br><a  target=\"_top\" href=\"visit.html?type="+type+"&num="+i+"\">Visit</a>").openPopup().addTo(markers[type]);

        marker.on('click',function(e) {
            //console.log(e.target.options.mypopup)
            $("#popup").html(e.target.options.mypopup);
            $("#popup").show()
        });

        marker.addTo(markers);

        minLat = Math.min(minLat, lat);
        minLon = Math.min(minLon, lon);
        maxLat = Math.max(maxLat, lat);
        maxLon = Math.max(maxLon, lon)
    }

    mymap.addLayer(markers)
}


var currentDestination = {};


var next_step = false;

function drawStartEndPlacemarks() {

    startM.lat = prevLat;
    startM.lng = prevLon;

    endM.lat = currentDestination.place.geometry.coordinates[1];
    endM.lng = currentDestination.place.geometry.coordinates[0];


    L.marker(startM,{icon:startM.icon}).addTo(mymap);
    L.marker(endM,{icon:endM.icon}).addTo(mymap);
    selectMarkers(pois,mymap.getBounds());
}


var path=null;

var prevStart = "0,0";
function computeRoute() {

    var start = prevLat+","+prevLon;
    var end = currentDestination.place.geometry.coordinates[1] + "," + currentDestination.place.geometry.coordinates[0];

    console.log(start);
    console.log(end);

    if(getDistanceFromLatLonInM(start.split(",")[0],start.split(",")[1],prevStart.split(",")[0],prevStart.split(",")[1]) > 100) {
        console.log("recompute route");
        //$.getJSON('https://graphhopper.com/api/1/route?' +
        //        'vehicle=foot&locale=en-US&key=e32cc4fb-5d06-4e90-98e2-3331765d5d77&instructions=false&points_encoded=false' +
        //        '&point=' + newstart + '&point=' + end, function (data, status) {
        $.getJSON(conf.dita_server + 'route?vehicle=foot&start=' + start + '&end=' + end, function (data, status) {
            prevStart = start;
            var geojson = {
                //data: data.paths[0].points, // <--- use this if calling graphhopper.com
                data: data.points,
                style: {
                    fillColor: "green",
                    weight: 2,
                    opacity: 1,
                    color: 'blue',
                    dashArray: '3',
                    fillOpacity: 0.7
                }
            };
            if (path != null)
                mymap.removeLayer(path);
            path = L.geoJSON(geojson.data, {style: geojson.style});
            path.addTo(mymap);

        });
    }
    window.setTimeout(computeRoute,REROUTE_EVERY)
}

