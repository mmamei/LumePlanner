
var CROWD_TYPES = {
    ABS: 0,
    REL: 1
};

var cutoff = {
    ABS : [1000,500,200,100,0],
    REL : [10,5,2,1,0]
};

var colors = {
    ABS : ['#ffffb2','#fecc5c','#fd8d3c','#f03b20','#bd0026'],
    REL : ['#d7191c','#fdae61','#ffffbf','#abd9e9','#2c7bb6']
};

var mymap;
var city = window.sessionStorage.getItem("city");
var cityLonLatBbox = JSON.parse(window.sessionStorage.getItem("citybbox"));
var pois = JSON.parse(window.sessionStorage.getItem("pois"));
var mIcons = JSON.parse(window.sessionStorage.getItem("mIcons"));
var user_prefs = JSON.parse(window.sessionStorage.getItem("preferences"));
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

var MAP_TYPES = {
    MAP: 0,
    NEXT_STEP: 1,
    CROWD: 2
};

var map_type = MAP_TYPES.MAP;
if(checkNextStep()) map_type = MAP_TYPES.NEXT_STEP;
if(getUrlParameter("crowd")) map_type = MAP_TYPES.CROWD;

var path=null;
var path_coords = null;


function checkNextStep() {
    var vp = JSON.parse(window.sessionStorage.getItem("visitplan"));
    if(vp == null) return false;
    var it = vp.plans[JSON.parse(window.sessionStorage.getItem("type_of_plan"))];
    if(!it) {
        //alert("broken plan")
        return false;
    }
    return true;
}



function localize(position) {
    var accuracy = position.coords.accuracy;
    var lat = position.coords.latitude;
    var lng = position.coords.longitude;
    window.sessionStorage.setItem("lat",lat);
    window.sessionStorage.setItem("lng",lng);

    var inCity = cityLonLatBbox[0] <= lng && lng <= cityLonLatBbox[2] &&
        cityLonLatBbox[1] <= lat && lat <= cityLonLatBbox[3];
    //console.log(cityLonLatBbox);
    //console.log("localized at " + lat + "," + lng);
    //console.log(inCity);
    if(map_type != MAP_TYPES.NEXT_STEP && !inCity && !out_city_alert_fired) {
        out_city_alert_fired = true;
        alert("Sei ancora troppo lontano dalla città per posizionarti sulla mappa")
    }


    if (centerMarker == null) {
        centerMarker = L.marker([lat, lng], {icon: centerIcon}).addTo(mymap);
        if(map_type == MAP_TYPES.NEXT_STEP) mymap.setZoom(18);
        else mymap.setZoom(16);
        getCrowdedPOIS()
    }
    centerMarker.setLatLng([lat, lng]);
    if((map_type == MAP_TYPES.NEXT_STEP && !dragged) || (map_type != MAP_TYPES.NEXT_STEP && !dragged && inCity))
        mymap.panTo([lat, lng]);

    if(map_type != MAP_TYPES.NEXT_STEP && !dragged && !inCity)
        mymap.fitBounds([
            [cityLonLatBbox[1], cityLonLatBbox[0]],
            [cityLonLatBbox[3], cityLonLatBbox[2]]
        ]);

    //console.log("localized at (" + lat + "," + lng+") accuracy = "+accuracy);
    if(getDistanceFromLatLonInM(lat,lng,prevLat,prevLon) > SEND_POSITION_EVERY_METERS) {
        $.getJSON(conf.dita_server + 'localize?lat=' + lat + "&lon=" + lng + "&user=" + window.localStorage.getItem("user"), function (data, status) {
        });
        prevLat = lat;
        prevLon = lng;
    }


    selectMarkers();
    if(map_type == MAP_TYPES.NEXT_STEP) {
        var dist = getDistanceFromLatLonInM(currentDestination.geometry.coordinates[1], currentDestination.geometry.coordinates[0], lat, lng);
        $("#dist").html("mancano " + dist.toFixed(0) + " metri");
        computeRoute()
    }
}




var markers = new L.LayerGroup();
var crowded_markers = new L.LayerGroup();


function selectMarkers() {
    if(map_type == MAP_TYPES.MAP || map_type == MAP_TYPES.NEXT_STEP) poiMarkers();
    if(map_type == MAP_TYPES.CROWD) crowdMarkers();
}

function poiMarkers() {
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

        var aimp = a.importance + 2 * user_prefs[a.category];
        var bimp = b.importance + 2 * user_prefs[b.category];

        if(aimp < bimp) return 1;
        if(aimp > bimp) return -1;
        return 0
    });


    var num = Math.min(MAX_POIS_IN_MAP,visiblePois.length);

    for(var i=0; i<num;i++) {
        var x = visiblePois[i];
        var type = x.category;
        var lat = x.geometry.coordinates[1];
        var lon = x.geometry.coordinates[0];
        var id = x.place_id;


        var info =
            "<div>"+format_name(x.display_name)+"</div>" +
            "<div>"+
            "<span place='"+type+"__"+id+"' onclick='visit()' class='ui-btn ui-shadow ui-corner-all ui-icon-carat-r ui-btn-icon-right ui-btn-active ui-state-persist'>Altre Informazioni</span>&nbsp;" +
            "<span onclick='$(\"#popup\").hide()' class='ui-btn ui-btn-b ui-shadow ui-corner-all ui-icon-delete ui-btn-icon-right ui-btn-active ui-state-persist'>Chiudi</span>" +
            "</div>"+
            "<div style='color:lightsteelblue'>"+format_name_from(x.display_name)+": "+x.type+"</div>";



        var icon = markerIcons[type];
        if(!icon) icon = markerIcons["attractions"];


        var marker = L.marker([lat, lon], {icon: icon, mypopup:info});

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
    //console.log(count)
}

function getCrowdedPOIS() {

        $.getJSON(conf.dita_server_files+'data/'+city+"/crowd.json", function (data, status) {
            //console.log("********* getting crowded pois *********");

            crowded_markers.clearLayers();
            crowded_markers = new L.LayerGroup();

            for (var i = 0; i < data.nrows; i++)
                for (var j = 0; j < data.ncols; j++) {
                    if(data.avalues[i][j] > 100 && data.mvalues[i][j] > 2) {
                        var border = getCellBorder(i, j, data.ox, data.oy, data.xdim, data.ydim, 1);

                        L.polygon(border,{
                            avalue: data.avalues[i][j],
                            mvalue: data.mvalues[i][j]
                        }).setStyle({
                            fillColor: getColor(data.mvalues[i][j], CROWD_TYPES.REL),
                            weight: 1,
                            opacity: 1,
                            color: 'white',
                            fillOpacity: 0.5
                        }).on('click',function(e) {
                            //console.log(e.target.options.mypopup)
                            var info =
                                "<div><h4>Area Affollata</h4>In questa zona ci sono circa <strong>"+e.target.options.avalue+"</strong> persone!<br>E' circa </strong>"+e.target.options.mvalue.toFixed(0)+"</strong> volte il numero di persone abituale</div>" +
                                "<div onclick='$(\"#popup\").hide()' class='ui-btn ui-btn-b ui-shadow ui-corner-all ui-icon-delete ui-btn-icon-right ui-btn-active ui-state-persist'>Chiudi</div>";

                            $("#popup").html(info);
                            $("#popup").show()
                        }).addTo(crowded_markers);
                    }
                }
            mymap.addLayer(crowded_markers);
        });
    setTimeout(getCrowdedPOIS,1*60*1000)
}


var crowd_type = CROWD_TYPES.ABS;

function crowdTime2String(time) {
    var y = time.substring(0,4);
    var m = time.substring(4,6);
    var d = time.substring(6,8);
    var h = time.substring(9,11);
    var min = time.substring(11,13);
    return d+"/"+m+"/"+y+" "+h+":"+min
}

function nlegend(crowd_type) {
    var grades = crowd_type == CROWD_TYPES.ABS ? cutoff.ABS.slice() : cutoff.REL.slice(); // slice() copy by value
    grades.reverse();
    var new_legend = "<h4>"+crowdTime2String(crowd.time)+"</h4>";
    for (var i = 0; i < grades.length; i++) {
        //console.log(grades[i]+" ==> "+getColor(grades[i] + 0.01,crowd_type))
        new_legend += "<i style='background:" + getColor(grades[i] + 0.01,crowd_type) + "'></i>" +
            grades[i] + (grades[i + 1] ? '&ndash;' + grades[i + 1] + '<br>' : '+');
    }
    $("#leg").html(new_legend);
    var switch_button = crowd_type == CROWD_TYPES.ABS ? "REL" : "ABS";
    $("#legend_switch").text("switch to "+switch_button)
}



var crowd  = null;




function crowdMarkers() {

    if(crowd == null) {

        $.getJSON(conf.dita_server_files+'data/'+city+"/crowd.json", function (data, status) {
            crowd = data;
            console.log(crowd);

            var legend = L.control({position: 'bottomright'});
            legend.onAdd = function (map) {
                var div = L.DomUtil.create('div', 'info legend');
                div.innerHTML = "<div id='leg'></div><br><button id='legend_switch'>switch to REL</button>";
                return div;
            };
            legend.addTo(mymap);
            nlegend(crowd_type);

            $("#legend_switch").click(function() {
                if(crowd_type == CROWD_TYPES.ABS) {
                    crowd_type = CROWD_TYPES.REL;
                    nlegend(crowd_type)
                }
                else {
                    crowd_type = CROWD_TYPES.ABS;
                    nlegend(crowd_type)
                }
            });
            crowdMarkers()
        })
    }
    else {
        markers.clearLayers();
        markers = new L.LayerGroup();

        var z = mymap.getZoom();
        var size = z > 14 ? 1 :
            z > 13 ? 2 : 4;

        var max = -1000;


        var bbox = mymap.getBounds();

        for (var i = 0; i < crowd.nrows; i = i + size)
            for (var j = 0; j < crowd.ncols; j = j + size) {

                var v = 0;
                var num = 0;
                var den = 0;
                for (var i1 = i; i1 < (i + size); i1++)
                    for (var j1 = j; j1 < (j + size); j1++) {

                        var x = crowd_type == CROWD_TYPES.ABS ? crowd.avalues[i][j] : crowd.mvalues[i][j];

                        max = Math.max(x,max);
                        if (x > 0) {
                            num += x;
                            den++
                        }
                    }

                v = (crowd_type == CROWD_TYPES.ABS) ? num :  (den > 0) ? num / den  : -1;



                if (bbox && v > 0) {
                    var border = getCellBorder(i, j, crowd.ox, crowd.oy, crowd.xdim, crowd.ydim, size);
                    var ll = bbox.getSouthWest();
                    var tr = bbox.getNorthEast();
                    var visible = false;
                    for(var k=0; k<border.length;k++) {
                        var lat = border[k][0];
                        var lon = border[k][1];
                        if (ll.lat < lat && lat < tr.lat && ll.lng < lon && lon < tr.lng) {
                            visible = true;
                            break;
                        }
                    }
                    if(visible) {
                        L.polygon(border).setStyle({
                            fillColor: getColor(v, crowd_type),
                            weight: 1,
                            opacity: 1,
                            color: 'white',
                            fillOpacity: 0.5
                        }).addTo(markers);
                    }
                }
            }

        console.log("maximum value overall "+max);
        /*
        var count= 0;
        markers.eachLayer(function(marker) {count++});
        console.log("num visible squares "+count)
        */
        markers.addTo(mymap)
    }
}




function getColor(d, crowd_type) {
    var xcutoff = crowd_type == CROWD_TYPES.ABS ? cutoff.ABS : cutoff.REL;
    var palette = crowd_type == CROWD_TYPES.ABS ? colors.ABS : colors.REL;

    return  d > xcutoff[0] ? palette[0] :
            d > xcutoff[1] ? palette[1] :
            d > xcutoff[2] ? palette[2] :
            d > xcutoff[3] ? palette[3] :
                             palette[4];
}

function getCellBorder(i, j, ox, oy, xdim, ydim, size) {
    var ll = [];

    // bottom left corner
    var x = ox + (j * xdim) - xdim/2;
    var y = oy - (i * ydim) - ydim/2;

    ll.push([y,x]);
    ll.push([y,x+size*xdim]);
    ll.push([y+size*ydim,x+size*xdim]);
    ll.push([y+size*ydim,x]);


    return ll;
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


    var next_icon = true;

    if(currentDestination && !clickedVisit)
        actualVisit = currentDestination;

    if(clickedVisit) {
        actualVisit = clickedVisit;
        if(!currentDestination || clickedVisit.place_id != currentDestination.place_id)
            next_icon = false;
    }

    console.log(actualVisit);

    var txt = "";
    txt += "<h2 id='title' align='center'>" + actualVisit.display_name.split(",")[0] + "</h2>";

    if (actualVisit.photo_url != null)
        txt += "<img src='" + actualVisit.photo_url + "' class='img-responsive center'>";

    txt += "<p>"+actualVisit.display_name.replace(",","<br/>")+"</p>";

    txt += "<div style='padding: 10px;text-align: justify'>";

    if(actualVisit.description != null)
        txt += actualVisit.description;
    else {
        var afo = aforismi[Math.floor(Math.random()*aforismi.length)];
        txt += "Da queste parti si dice: \"<i>" + afo + "</i>\""
    }

    txt += "<br><br></div>";

    if(actualVisit.www != null) {
        var href = actualVisit.www.trim();
        if(href.startsWith("\""))
            href = href.split(",")[0].substring(1);
        console.log("www ==> " +href);
        txt += "<a href='"+href+"' class='ui-btn ui-corner-all'>Altre Informazioni</a>"
    }


    if(next_icon)
        txt += "<div id='next' class='ui-btn ui-shadow ui-corner-all ui-icon-carat-r ui-btn-icon-right ui-btn-active ui-state-persist'><div style='margin:0px'>Prossima Visita</div></div>";
    txt += "<div onclick='$(\"#visit_popup\").hide();$(\"#popup\").hide()' class='ui-btn ui-btn-b ui-shadow ui-corner-all ui-icon-delete ui-btn-icon-right ui-btn-active ui-state-persist'><div style='margin:0px'>Chiudi</div></div>";

    txt += "<button style='width:300px;margin: 0 auto;' class='ui-btn ui-shadow ui-corner-all ui-btn-active ui-state-persist' id='share_btn'>Condividi con  <i id='fb' class='fa fa-facebook-f'></i>acebook </button>";


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
                //place: actualVisit.place_id,
                //coordinates: JSON.stringify({
                //    'latitude' : actualVisit.geometry.coordinates[1],
                //    'longitude' : actualVisit.geometry.coordinates[0]
                //}),
                link: "https://play.google.com/store/apps/details?id=it.unimore.morselli.lume"
            },
            success: function() {
                alert('Informazione condivisa su Facebook!');
            },
            error: function(err) {
                //alert(actualVisit.geometry.coordinates[1]+","+actualVisit.geometry.coordinates[0])
                //alert(JSON.stringify(err))
                window.location = "fb.html"
            }
        });
    });

    $("#next").click(function() {

        $("#visit_popup").hide();
        $("#popup").hide();


        //if(!currentVisit) {
        var user = window.localStorage.getItem("user");
        var city = window.sessionStorage.getItem("city");
        var d = new Date().getTime();
        var request = {
            user: user,
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
        console.log(items.to_visit.length);
        if((items.to_visit.length) > 0)
            $("#missing_stops").html("mancano altre "+(items.to_visit.length)+" tappe")
    }
    window.sessionStorage.setItem("currentDestination",JSON.stringify(currentDestination));
}





