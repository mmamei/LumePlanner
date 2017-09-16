var mymap;

var pois;
var markers = new L.LayerGroup();
var mIcons;

var minLat = 1000;
var minLon = 1000;
var maxLat = -1000;
var maxLon = -1000;

var dragged = false;
var centerMarker = null;
var prevLat = 0;
var prevLon = 0;
function localize(position) {

    if(getDistanceFromLatLonInM(position.coords.latitude,position.coords.longitude,prevLat,prevLon) > SEND_POSITION_EVERY_METERS) {
        $.getJSON(conf.dita_server + 'localize?lat=' + position.coords.latitude + "&lon=" + position.coords.longitude + "&user=" + JSON.parse(window.localStorage.getItem("user")).email, function (data, status) {
        });
        console.log("localized at " + position.coords.latitude + "," + position.coords.longitude);
        prevLat = position.coords.latitude;
        prevLon = position.coords.longitude;
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

        var info = "<span class='popcontent'>"+format_name(x.display_name)+"<br></span>" +
                    "<a  target=\"_top\" href=\"visit.html?type="+type+"&num="+i+"\">Visit</a><br>" +
                    "<span style='color:lightsteelblue'>"+format_name_from(x.display_name)+":"+x.place_id+"</span><br>"+
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



$(document).ready(function(){


    mIcons = JSON.parse(window.sessionStorage.getItem("mIcons"));

    $("#popup").hide();
    pois = JSON.parse(window.sessionStorage.getItem("pois"));
    console.log(pois);


    mymap = L.map('mapid',{
        attributionControl: false,
        scrollWheelZoom: true,
        doubleClickZoom: true,
        zoomControl: true,
        touchZoom: true,
        dragging: true
    });
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png").addTo(mymap);
    selectMarkers(pois);
    mymap.fitBounds([
        [minLat, minLon],
        [maxLat, maxLon]
    ]);


    L.easyButton('fa-crosshairs fa-lg', function(btn, map) {
        dragged = false;
    }).addTo(mymap);



    mymap.on('drag', function(e) {
        dragged = true;
        selectMarkers(pois,mymap.getBounds())
    });

    mymap.on('zoom', function(e) {
        selectMarkers(pois,mymap.getBounds())
    });






    if(conf.localize)
        if(navigator.geolocation)
            navigator.geolocation.getCurrentPosition(localize);
        else
            alert("Turn on GPS");

    $("#itinerary").click(function(){
        $(this).css("opacity","0.5");
        var data = JSON.parse(window.sessionStorage.getItem("itineraries"));
        if(data == null)
            window.location.href = "itinerary_create.html";
        else
            window.location.href = "itineraries.html";
    });


    $("#bus").click(function(){
        console.log("go to travelplanner");
        window.location.href = "http://travelplanner.cup2000.it";

    })

});