
function PlaceTime(place,time) {
    this.place = place;
    this.time = time;
}

function computeDistance(lat1, lng1, lat2, lng2) {
    var earthRadius = 6371000; //meters
    var dLat = (lat2-lat1) * Math.PI / 180;
    var dLng = (lng2-lng1) * Math.PI / 180;
    var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.cos(lat1 * Math.PI / 180) *
        Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLng/2) * Math.sin(dLng/2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    var dist = (earthRadius * c);
    return dist;
}


var items;
var mymap;
var start;
var end;




function drawStartEndPlacemarks() {
    var m = [];
    m[0] = {
        lat : parseFloat(start.split(',')[0]),
        lng: parseFloat(start.split(',')[1]),
        icon: L.divIcon({
            type: 'div',
            className: 'marker',
            html: "<span class=\"fa-col-blue fa-stack fa-lg\"><i class=\"fa fa-home fa-stack-2x\"></i></span>"
        })
    };

    m[1] = {
        lat : parseFloat(end.split(',')[0]),
        lng: parseFloat(end.split(',')[1]),
        icon: L.divIcon({
            type: 'div',
            className: 'marker',
            html: "<span class=\"fa-col-green fa-stack fa-lg\"><i class=\"fa fa-flag-checkered fa-stack-2x\"></i></span>"
        })
    };
    for(var i=0; i<m.length;i++) {
        var marker = L.marker(m[i],{icon:m[i].icon}).addTo(mymap);
    }

    var pois = JSON.parse(window.sessionStorage.getItem("pois"));
    var markers = new Markers();
    for(var type in pois) {
        console.log(type);
        if (pois[type])
            for (var i = 0; i < pois[type].length; i++) {
                var x = pois[type][i];
                var lat = x.geometry.coordinates[1];
                var lon = x.geometry.coordinates[0];
                var marker = L.marker([lat, lon], {icon: markerIcons[type]});
                marker.bindPopup(format_name(x.display_name)+"<br><a  target=\"_top\" href=\"visit.html?type="+type+"&num="+i+"\">Visit</a>").openPopup().addTo(markers[type]);
            }
    }
    for(k in markers)
        mymap.addLayer(markers[k])
    L.control.layers(null,markers).addTo(mymap);
}


/****************************************************************************************************/
/****************************************************************************************************/
/*                                        SIMULATED MOVEMENT                                        */
/****************************************************************************************************/
/****************************************************************************************************/

function simulatedMovement() {
    //GH alt_key: LijBPDQGfu7Iiq80w3HzwB4RUDJbMbhs6BU0dEnn
    $.getJSON('https://graphhopper.com/api/1/route?' +
        'vehicle=foot&locale=en-US&key=e32cc4fb-5d06-4e90-98e2-3331765d5d77&instructions=false&points_encoded=false' +
        '&point=' + start + '&point=' + end, function (data, status) {

        //routing API
        console.log(data.paths[0]);
        var geoJsonPoints = data.paths[0].points.coordinates;
        var time_tot = data.paths[0].time;

        var distances = [];
        var d_tot = 0.0;
        var i, p_from, p_to, d;
        for (i = 0; i < geoJsonPoints.length - 1; i += 1) {
            p_from = geoJsonPoints[i];
            p_to = geoJsonPoints[i + 1];
            d = computeDistance(p_from[1], p_from[0], p_to[1], p_to[0]);
            distances.push(d);
            d_tot += d;
        }

        var d_rates = [];
        for (i = 0; i < distances.length; i += 1) {
            d_rates.push(distances[i] / d_tot);
        }

        var start_time = new Date().getTime();
        var timings = [];
        timings.push(start_time);
        for (i = 0; i < d_rates.length; i += 1) {
            start_time += parseInt(d_rates[i] * time_tot, 10);
            timings.push(start_time);
        }


        var adaptedGeoJsonData = {
            type: "Feature",
            geometry: {
                type: "MultiPoint",
                coordinates: geoJsonPoints
            },
            properties: {
                time: timings
            }
        };
        console.log({adaptedGeoJson: adaptedGeoJsonData});

        var timed_update;
        var old_position;


        function playback(adaptedGeoJsonData) {

            // ***** This part place a placemark according to user simulated movement

            var options = {
                //tickLen : '', //millis [def:250]
                speed : 10.0, //float multiplier [def:1]
                tracksLayer : false,
                marker: {
                    icon: L.divIcon({
                        type: 'div',
                        className: 'marker',
                        html: "<span class=\"fa-col-blue\"><i class=\"fa fa-dot-circle-o fa-3x fa-rotate-dyn\"></i></span>"
                    })
                }
            };
            var v = new L.Playback(mymap, adaptedGeoJsonData, null, options);
            v.start();

            // ***** this part centers the map where is the previous placemerk

            timed_update = setInterval(function() {
                var latlng = mymap.layerPointToLatLng(mymap.getPanes().markerPane.childNodes[0]._leaflet_pos);
                //console.log(latlng)
                if (old_position && old_position===latlng) {
                    console.log("qui");
                }
                old_position = latlng;
                var dist =  computeDistance(parseFloat(end.split(',')[0]), parseFloat(end.split(',')[1]), latlng.lat, latlng.lng);
                //console.log(dist);
                if ( dist < 50) {
                    //changeView
                    clearInterval(timed_update);
                    if (items.to_visit.length > 0) {
                        window.location.href = "visit.html"
                    } else {
                        window.location.href = "overview.html"
                    }
                } else {
                    //console.log("moving:"+computeDistance(parseFloat(end.split(',')[0]), parseFloat(end.split(',')[1]), latlng.lat, latlng.lng));
                    mymap.panTo(L.latLng(latlng));
                }
            }, 100);
        }


        playback(adaptedGeoJsonData);



        drawStartEndPlacemarks();

        var geojson = {
            data: data.paths[0].points,
            style: {
                fillColor: "green",
                weight: 2,
                opacity: 1,
                color: 'blue',
                dashArray: '3',
                fillOpacity: 0.7
            }
        };

        L.geoJSON(geojson.data, {style: geojson.style}).addTo(mymap);

    });
}


/****************************************************************************************************/
/****************************************************************************************************/
/*                                             REAL MOVEMENT                                        */
/****************************************************************************************************/
/****************************************************************************************************/


var centerMarker = null;


var path=null;
function computeRoute() {
    console.log("recomute route");
    var newstart = centerMarker.getLatLng();
    newstart = newstart.lat+","+newstart.lng;
    //$.getJSON('https://graphhopper.com/api/1/route?' +
    //        'vehicle=foot&locale=en-US&key=e32cc4fb-5d06-4e90-98e2-3331765d5d77&instructions=false&points_encoded=false' +
    //        '&point=' + newstart + '&point=' + end, function (data, status) {
    $.getJSON(conf.dita_server + 'route?vehicle=foot&start=' + start + '&end='+end,function (data, status) {
        //console.log(data)
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
        if(path!=null)
            mymap.removeLayer(path);
        path = L.geoJSON(geojson.data, {style: geojson.style});
        path.addTo(mymap);

    });
    window.setTimeout(computeRoute,REROUTE_EVERY)
}

var dragged = false;
function localize(position) {
    $.getJSON(conf.dita_server + 'localize?lat=' + position.coords.latitude + "&lon="+ position.coords.longitude+"&user=" + JSON.parse(window.sessionStorage.getItem("user")).email, function (data, status) {});
    console.log("localized at "+position.coords.latitude+","+position.coords.longitude);
    if(!dragged)
        mymap.panTo([position.coords.latitude, position.coords.longitude]);
    if(centerMarker == null) {

        var icon = L.divIcon({
            type: 'div',
            className: 'marker',
            html: "<span class=\"fa-col-blue\"><i class=\"fa fa-dot-circle-o fa-3x fa-rotate-dyn\"></i></span>"
        });
        centerMarker = L.marker([position.coords.latitude, position.coords.longitude], {icon: icon}).addTo(mymap);
        mymap.setZoom(15);

        drawStartEndPlacemarks();
        computeRoute()
    }


    centerMarker.setLatLng([position.coords.latitude, position.coords.longitude]);



    window.setTimeout(function(){navigator.geolocation.getCurrentPosition(localize)},LOCALIZE_EVERY)

}



$(document).ready(function(){

    var visitplan = JSON.parse(window.sessionStorage.getItem("visitplan"));
    var type_of_plan = JSON.parse(window.sessionStorage.getItem("type_of_plan"));

    console.log(JSON.stringify(visitplan));
    console.log(type_of_plan);


    items = visitplan.plans[type_of_plan];
    console.log(items);


    var currentDeparture = {};
    var currentDestination = {};

    if (items.visited === null || items.visited.length === 0) {
        start = items.departure.geometry.coordinates[1]+','+items.departure.geometry.coordinates[0];
        end = items.to_visit[0].visit.geometry.coordinates[1]+','+items.to_visit[0].visit.geometry.coordinates[0];
        currentDeparture = new PlaceTime(items.departure, items.departure_time);
        currentDestination = new PlaceTime(items.to_visit[0].visit, items.to_visit[0].arrival_time);
    } else if (items.to_visit.length > 0) {
        start = items.visited[items.visited.length-1].visit.geometry.coordinates[1]+','+items.visited[items.visited.length-1].visit.geometry.coordinates[0];
        end = items.to_visit[0].visit.geometry.coordinates[1]+','+items.to_visit[0].visit.geometry.coordinates[0];
        currentDeparture = new PlaceTime(items.visited[items.visited.length-1].visit, items.visited[items.visited.length-1].departure_time);
        currentDestination = new PlaceTime(items.to_visit[0].visit, items.to_visit[0].arrival_time);
    } else {
        start = items.visited[items.visited.length-1].visit.geometry.coordinates[1]+','+items.visited[items.visited.length-1].visit.geometry.coordinates[0];
        end = items.arrival.geometry.coordinates[1]+','+items.arrival.geometry.coordinates[0];
        currentDeparture = new PlaceTime(items.visited[items.visited.length-1].visit, items.visited[items.visited.length-1].departure_time);
        currentDestination = new PlaceTime(items.arrival, items.arrival.arrival_time);
    }

    console.log("start:"+start);
    console.log("destination:"+end);


    window.sessionStorage.setItem("currentDestination",JSON.stringify(currentDestination));


    mymap = L.map('mapid',{
        attributionControl: false,
        scrollWheelZoom: true,
        doubleClickZoom: true,
        zoomControl: true,
        touchZoom: true,
        dragging: true,
    });

    L.easyButton('fa-crosshairs fa-lg', function(btn, map) {
        dragged = false;
    }).addTo(mymap);


    mymap.on('drag', function(e) {
        dragged = true;
    });

    mymap.setView([parseFloat(start.split(',')[0]), parseFloat(start.split(',')[1])], 18);
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png").addTo(mymap);


    if(conf.localize && navigator.geolocation)
        navigator.geolocation.getCurrentPosition(localize);
    else
        simulatedMovement();


    $("#visit").click(function(){
        if (items.to_visit.length > 0) {
            window.location.href = "visit.html"
        } else {
            window.location.href = "finish.html"
        }
    });





});