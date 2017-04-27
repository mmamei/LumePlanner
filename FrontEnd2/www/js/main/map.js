var mymap;



var dragged = false;
var centerMarker = null;
function localize(position) {

        console.log("localized at " + position.coords.latitude + "," + position.coords.longitude);
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


        window.setTimeout(function () {
            navigator.geolocation.getCurrentPosition(localize)
        }, 1000)
}




$(document).ready(function(){





    var pois = JSON.parse(window.sessionStorage.getItem("pois"));

    console.log(pois);

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


    var markers = [];
    var minLat = 1000;
    var minLon = 1000;
    var maxLat = -1000;
    var maxLon = -1000;

    for(var type in pois) {
        console.log(type);
        if(pois[type])
            for (var i = 0; i < pois[type].length; i++) {
                var x = pois[type][i];
                var lat = x.geometry.coordinates[1];
                var lon = x.geometry.coordinates[0];
                var marker = L.marker([lat, lon], {icon: markerIcons[type]}).addTo(mymap);
                marker.bindPopup(format_name(x.display_name)+"<a  target=\"_top\" href=\"visit.html?type="+type+"&num="+i+"\">Visit</a>").openPopup();
                markers.push(marker);
                minLat = Math.min(minLat, lat);
                minLon = Math.min(minLon, lon);
                maxLat = Math.max(maxLat, lat);
                maxLon = Math.max(maxLon, lon)
            }
    }

    mymap.fitBounds([
        [minLat, minLon],
        [maxLat, maxLon]
    ]);

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png").addTo(mymap);


    if(conf.localize && navigator.geolocation)
        navigator.geolocation.getCurrentPosition(localize);


    $("#itinerary").click(function(){
        window.location.href = "home.html";
    })

});