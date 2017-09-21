

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
var destination_marker = null;
var visitplan;
var type_of_plan;
var items;

var cityLonLatBbox = JSON.parse(window.sessionStorage.getItem("citybbox"));
var out_city_alert_fired = false;


var currentDestination = {};
var next_step = window.sessionStorage.getItem("visitplan") ? true : false;


var path=null;
var path_coords = null;


function localize(position) {
    var accuracy = position.coords.accuracy;
    var lat = position.coords.latitude;
    var lng = position.coords.longitude;


    var inCity = cityLonLatBbox[0] <= lng && lng <= cityLonLatBbox[2] &&
        cityLonLatBbox[1] <= lat && lat <= cityLonLatBbox[3];
    console.log(cityLonLatBbox);
    console.log("localized at " + lat + "," + lng);
    console.log(inCity);
    if(!next_step && !inCity && !out_city_alert_fired) {
        out_city_alert_fired = true;
        alert("Sei ancora troppo lontano dalla città per posizionarti sulla mappa")
    }


    if (centerMarker == null) {
        centerMarker = L.marker([lat, lng], {icon: centerIcon}).addTo(mymap);
        mymap.setZoom(18)
    }
    centerMarker.setLatLng([lat, lng]);
    if((next_step && !dragged) || (!next_step && !dragged && inCity))
        mymap.panTo([lat, lng]);

    if(!next_step && !dragged && !inCity)
        mymap.fitBounds([
            [cityLonLatBbox[1], cityLonLatBbox[0]],
            [cityLonLatBbox[3], cityLonLatBbox[2]]
        ]);

    console.log("localized at (" + lat + "," + lng+") accuracy = "+accuracy);
    if(getDistanceFromLatLonInM(lat,lng,prevLat,prevLon) > SEND_POSITION_EVERY_METERS) {
        $.getJSON(conf.dita_server + 'localize?lat=' + lat + "&lon=" + lng + "&user=" + JSON.parse(window.localStorage.getItem("user")).email, function (data, status) {
        });
        prevLat = lat;
        prevLon = lng;
    }


    selectMarkers();
    if(next_step) {
        var dist = getDistanceFromLatLonInM(currentDestination.geometry.coordinates[1], currentDestination.geometry.coordinates[0], lat, lng);
        $("#dist").html("mancano " + dist.toFixed(0) + " metri");
        computeRoute()
    }
}




var markers = new L.LayerGroup();
function selectMarkers() {

    var bbox = mymap.getBounds();

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
    }

    mymap.addLayer(markers);

    if(currentDestination.geometry) {
        if (destination_marker == null) {
            destination_marker = L.marker([currentDestination.geometry.coordinates[1], currentDestination.geometry.coordinates[0]], {icon: endIcon}).addTo(mymap);
        }
        destination_marker.setLatLng([currentDestination.geometry.coordinates[1], currentDestination.geometry.coordinates[0]])
    }
    var count= 0;
    markers.eachLayer(function(marker) {count++});
    console.log(count)
}

function computeRoute() {

    var start = prevLat+","+prevLon;
    var end = currentDestination.geometry.coordinates[1] + "," + currentDestination.geometry.coordinates[0];

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
        actualVisit = currentDestination;
        close_icon = false
    }

    if(clickedVisit) {
        actualVisit = clickedVisit;
        if(!currentDestination || clickedVisit.place_id != currentDestination.place_id)
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
            visited: currentDestination,
            time: d,
            rating: 10, // ???
            city: city
        };
        console.log(request);

        $.postJSON(conf.dita_server + "visited", request, function (data, status) {

            window.sessionStorage.setItem("visitplan", JSON.stringify(data));
            setupDestination();
        });

    });

}





function setupDestination() {
    visitplan = JSON.parse(window.sessionStorage.getItem("visitplan"));
    type_of_plan = JSON.parse(window.sessionStorage.getItem("type_of_plan"));

    //console.log(JSON.stringify(visitplan));
    //console.log(type_of_plan);

    items = visitplan.plans[type_of_plan];
    console.log(items);

    if (items.visited === null || items.visited.length === 0)
        currentDestination = items.to_visit[0].visit;
    else if (items.to_visit.length > 0)
        currentDestination = items.to_visit[0].visit;
    else
        currentDestination = items.arrival;

    var name = format_name(currentDestination.display_name);
    console.log(name);
    if(name == "Current Location") {
        $("#destination").html("Ritorna al punto di partenza");
        $("#missing_stops").html("")
    }
    else {
        $("#destination").html("Destinazione: "+name);
        if((items.to_visit.length-1) > 0)
            $("#missing_stops").html("mancano altre "+(items.to_visit.length-1)+" tappe")
    }
    window.sessionStorage.setItem("currentDestination",JSON.stringify(currentDestination));
}





