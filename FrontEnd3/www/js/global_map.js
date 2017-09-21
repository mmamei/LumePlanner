

function PlaceTime(place,time) {
    this.place = place;
    this.time = time;
}

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


var startIcon = L.divIcon({
    type: 'div',
    className: 'marker',
    html: "<span class=\"fa-col-blue fa-stack fa-lg\"><i class=\"fa fa-home fa-stack-2x\"></i></span>"
});

var endIcon = L.divIcon({
    type: 'div',
    className: 'marker',
    html: "<span class='fa-stack fa-lg'>" +
    "<i class='fa fa-circle fa-stack-2x'></i> " +
    "<i class='fa fa-flag fa-stack-1x fa-inverse'></i> " +
    "</span>"
});

var centerIcon = L.divIcon({
    type: 'div',
    className: 'marker',
    html: "<span class=\"fa-col-blue\"><i class=\"fa fa-dot-circle-o fa-3x fa-rotate-dyn\"></i></span>"
});

var pathStyle = {
    fillColor: "green",
    weight: 5,
    opacity: 1,
    color: 'blue',
    dashArray: '3',
    fillOpacity: 0.7
};

var REROUTE_EVERY = 20000;
var SEND_POSITION_EVERY_METERS = 20;
var MAX_POIS_IN_MAP = 10;

var dragged = false;
var centerMarker = null;
var endMarker = null;


var visitplan;
var type_of_plan;
var items;

var cityLonLatBbox = JSON.parse(window.sessionStorage.getItem("citybbox"));
var out_city_alert_fired = false;

function localize(position) {
    var accuracy = position.coords.accuracy;
    var lat = position.coords.latitude;
    var lng = position.coords.longitude;
    console.log("localized at (" + lat + "," + lng+") accuracy = "+accuracy);
    if(getDistanceFromLatLonInM(lat,lng,prevLat,prevLon) > SEND_POSITION_EVERY_METERS) {
        $.getJSON(conf.dita_server + 'localize?lat=' + lat + "&lon=" + lng + "&user=" + JSON.parse(window.localStorage.getItem("user")).email, function (data, status) {
        });
        prevLat = lat;
        prevLon = lng;
        window.sessionStorage.setItem("prevLat",prevLat);
        window.sessionStorage.setItem("prevLon",prevLon);
    }

    if (centerMarker == null) {
        centerMarker = L.marker([lat, lng], {icon: centerIcon}).addTo(mymap);
        mymap.setZoom(18)
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
        selectMarkers(pois,mymap.getBounds(),mymap.getZoom());
    else {
        drawStartEndPlacemarks();
        computeRoute()
    }

    //window.setTimeout(function () {
    //    navigator.geolocation.getCurrentPosition(localize)
    //}, LOCALIZE_EVERY)
}




var minLat = 1000;
var minLon = 1000;
var maxLat = -1000;
var maxLon = -1000;

var markers = new L.LayerGroup();
function selectMarkers(pois,bbox,zoom) {

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


    var num = Math.min(MAX_POIS_IN_MAP,visiblePois.length);

    for(var i=0; i<num;i++) {
        var x = visiblePois[i];
        var type = x.category;
        var lat = x.geometry.coordinates[1];
        var lon = x.geometry.coordinates[0];
        var id = x.place_id;


        var info = "<span class='popcontent'>"+format_name(x.display_name)+"<br></span>" +
            //"<a  target=\"_top\" href=\"visit.html?type="+type+"&id="+id+"\">Visit</a><br>" +

            "<span place='"+type+"__"+id+"' onclick='visit()'>Visit</span><br>" +
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

    mymap.addLayer(markers);

    if(currentDestination.place) {
        if (destination_marker == null) {
            destination_marker = L.marker([currentDestination.place.geometry.coordinates[1], currentDestination.place.geometry.coordinates[0]], {icon: endIcon}).addTo(mymap);
        }
        destination_marker.setLatLng([currentDestination.place.geometry.coordinates[1], currentDestination.place.geometry.coordinates[0]])
    }
    var count= 0;
    markers.eachLayer(function(marker) {count++});
    console.log(count)
}

/*
 <h2 id="title" align = "center"></h2>

 <img id="photo_url" src="" class="img-responsive center">
 <p id="info"></p>
 <p id="description"></p>
 <button id="www" class="btn btn-primary" tkey="more_info">Altre Informazioni</button>

 <div id="cities" class="ui-grid-a ui-responsive"></div>
 <button id="next" class="btn btn-primary" tkey="next_visit">Prossima Visita</button>
 <button class="btn btn-block" id="share_btn">Condividi con  <i id="fb" class="fa fa-facebook-f"></i>acebook </button>
 <button id="close" class="btn btn-primary" tkey="close_visit">Chiudi</button>
 */

function visit() {
    var clickedVisit = null;
    var place = $(event.target).attr("place");
    if(place) {
        var type_id = place.split("__");
        var type = type_id[0];
        var id = type_id[1];

        if (type && id) {
            var pois = JSON.parse(window.sessionStorage.getItem("pois"));
            for (var i = 0; i < pois[type].length; i++)
                if (pois[type][i].place_id == id) {
                    clickedVisit = pois[type][i];
                    break;
                }
        }
    }

    var currentDestination = JSON.parse(window.sessionStorage.getItem("currentDestination"));

    var actualVisit;

    var close_icon = true;
    var next_icon = true;

    if(currentDestination && !clickedVisit) {
        actualVisit = currentDestination.place;
        close_icon = false
    }

    if(clickedVisit) {
        actualVisit = clickedVisit;
        if(!currentDestination || clickedVisit.place_id != currentDestination.place.place_id)
            next_icon = false;
        else
            close_icon = false
    }

    var txt = "";
    txt += "<h2 id='title' align='center'>" + actualVisit.display_name.split(",")[0] + "</h2>";

    if (actualVisit.photo_url != null)
        txt += "<img src='" + actualVisit.photo_url + "' class='img-responsive center'>";

    txt += "<p>"+actualVisit.display_name.replace(",","<br/>")+"</p>";

    if(actualVisit.description != null)
        txt += "<p>"+actualVisit.description+"</p>";
    else {
        var afo = aforismi[Math.floor(Math.random()*aforismi.length)];
        txt += "<p>Da queste parti si dice: \"<i>" + afo + "</i>\"</p>"
    }

    if(actualVisit.www != null) {
        var href = actualVisit.www.trim();
        if(href.startsWith("\""))
            href = href.split(",")[0].substring(1);
        console.log("www ==> " +href);
        txt += "<a href='"+href+"' class='ui-btn ui-corner-all'>Altre Informazioni</a>"
    }

    txt += "<button class='ui-btn ui-corner-all' id='share_btn'>Condividi con  <i id='fb' class='fa fa-facebook-f'></i>acebook </button>";


    if(close_icon)
        txt += "<div onclick='$(\"#visit_popup\").hide();$(\"#popup\").hide()' class='ui-btn ui-shadow ui-corner-all ui-icon-delete ui-btn-icon-right ui-btn-active ui-state-persist'><div style='margin:0px'>Chiudi</div></div>";
    if(next_icon)
        txt += "<div id='next' class='ui-btn ui-shadow ui-corner-all ui-icon-carat-r ui-btn-icon-right ui-btn-active ui-state-persist'><div style='margin:0px'>Prossima Visita</div></div>";


    $("#visit_popup").html(txt);
    $("#visit_popup").show();
    $("#share_btn").click(function() {
        var place_name = $("#title").html();
        //alert(place_name)
        openFB.api({
            method: 'POST',
            path: '/me/feed',
            params: {
                message: "Ho scoperto "+place_name+" grazie a Lume Planner!",
                link: "https://play.google.com/store/apps/details?id=it.unimore.morselli.lume"
            },
            success: function() {
                alert('Informazione condivisa su Facebook');
            },
            error: function() {
                alert('Si è verificato un problema con la condivisione');
            }
        });
    });

    $("#next").click(function() {

        $("#visit_popup").hide();
        $("#popup").hide();


        //if(!currentVisit) {
        var user = JSON.parse(window.localStorage.getItem("user"));
        var city = window.sessionStorage.getItem("city");
        var d = new Date().getTime();
        var request = {
            user: user.email,
            visited: currentDestination.place,
            time: d,
            rating: 10, // ???
            city: city
        };
        console.log(request);

        $.postJSON(conf.dita_server + "visited", request, function (data, status) {

            window.sessionStorage.setItem("visitplan", JSON.stringify(data));
            setupDestination();
            if(!conf.localize && next_step)
               simulatedMovement();
        });

    });

}


var currentDestination = {};


var next_step = false;


var destination_marker = null;






function setupDestination() {
    visitplan = JSON.parse(window.sessionStorage.getItem("visitplan"));
    type_of_plan = JSON.parse(window.sessionStorage.getItem("type_of_plan"));

    //console.log(JSON.stringify(visitplan));
    //console.log(type_of_plan);

    items = visitplan.plans[type_of_plan];
    console.log(items);

    if (items.visited === null || items.visited.length === 0) {
        currentDestination = new PlaceTime(items.to_visit[0].visit, items.to_visit[0].arrival_time);
    } else if (items.to_visit.length > 0) {
        currentDestination = new PlaceTime(items.to_visit[0].visit, items.to_visit[0].arrival_time);
    } else {
        currentDestination = new PlaceTime(items.arrival, items.arrival.arrival_time);
    }

    var name = format_name(currentDestination.place.display_name);
    console.log(name);
    if(name == "Current Location") {
        $("#destination").html("Ritorna al punto di partenza");
        $("#missing_stops").html("")
    }
    else {
        $("#destination").html("Destinazione: "+name);
        $("#missing_stops").html("mancano altre "+(items.to_visit.length-1)+" tappe")
    }
    window.sessionStorage.setItem("currentDestination",JSON.stringify(currentDestination));
}

var path=null;
var path_coords = null;

function computeRoute() {

    var start = prevLat+","+prevLon;
    var end = currentDestination.place.geometry.coordinates[1] + "," + currentDestination.place.geometry.coordinates[0];

    //console.log(start);
    //console.log(end);

    //$.getJSON('https://graphhopper.com/api/1/route?' +
    //        'vehicle=foot&locale=en-US&key=e32cc4fb-5d06-4e90-98e2-3331765d5d77&instructions=false&points_encoded=false' +
    //        '&point=' + newstart + '&point=' + end, function (data, status) {
    $.getJSON(conf.dita_server + 'route?vehicle=foot&start=' + start + '&end=' + end, function (data, status) {
        path_coords = data.points;
        if (path != null)
            mymap.removeLayer(path);
        path = L.geoJSON(data.points, {style: pathStyle}).addTo(mymap);
    });
    if(conf.localize) window.setTimeout(computeRoute,REROUTE_EVERY)
}


var timed_update;
function simulatedMovement() {

    var start = prevLat+","+prevLon;
    var end = currentDestination.place.geometry.coordinates[1] + "," + currentDestination.place.geometry.coordinates[0];
    console.log(start);
    console.log(end);
    if(centerMarker == null)
        centerMarker = L.marker([prevLat,prevLon], {icon: centerIcon}).addTo(mymap);
    else centerMarker.setLatLng([prevLat,prevLon]);
    clearInterval(timed_update);

    computeRoute();

    var t = 0;
    timed_update = setInterval(function() {
        if(path_coords != null) {
            console.log(t + "/" + path_coords.coordinates.length);
            var lat = path_coords.coordinates[t][1];
            var lng = path_coords.coordinates[t][0];
            centerMarker.setLatLng([lat, lng]);
            mymap.panTo([lat, lng]);
            selectMarkers(pois, mymap.getBounds());
            prevLat = lat;
            prevLon = lng;
            window.sessionStorage.setItem("prevLat", prevLat);
            window.sessionStorage.setItem("prevLon", prevLon);
            var dist = getDistanceFromLatLonInM(parseFloat(end.split(',')[0]), parseFloat(end.split(',')[1]), lat, lng);
            $("#dist").html("mancano " + dist.toFixed(0) + " metri");
            t++;
            if (t == path_coords.coordinates.length) clearInterval(timed_update)
        }
    },2000)
}



/***************************************************************************************************************/
/************                                  KALMAN FILER                                           **********/
/*
var kMinAccuracy = 1;
var kQ_metres_per_second = 3;
var k_timestamp_milliseconds;
var klat;
var klng;
var kvariance;

function kalman(lat_measurement, lng_measurement, accuracy, time_stamp_milliseconds) {
    if (accuracy < kMinAccuracy) accuracy = kMinAccuracy;
    if (kvariance < 0) {
        // if variance < 0, object is unitialised, so initialise with current values
        k_timestamp_milliseconds = time_stamp_milliseconds;
        klat = lat_measurement;
        klng = lng_measurement;
        kvariance = accuracy*accuracy;
    } else {
        // else apply Kalman filter
        var TimeInc_milliseconds = time_stamp_milliseconds - k_timestamp_milliseconds;
        if (TimeInc_milliseconds > 0) {
            // time has moved on, so the uncertainty in the current position increases
            kvariance += TimeInc_milliseconds * kQ_metres_per_second * kQ_metres_per_second / 1000;
            k_timestamp_milliseconds = time_stamp_milliseconds;
        }

        // Kalman gain matrix K = Covarariance * Inverse(Covariance + MeasurementVariance)
        // NB: because K is dimensionless, it doesn't matter that variance has different units to lat and lng
        var K = kvariance / (kvariance + accuracy * accuracy);
        // apply K
        klat += K * (lat_measurement - klat);
        klng += K * (lng_measurement - klng);
        // new Covarariance  matrix is (IdentityMatrix - K) * Covarariance
        kvariance = (1 - K) * kvariance;
    }
}

*/


