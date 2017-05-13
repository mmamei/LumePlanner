var mymap;



var dragged = false;
var centerMarker = null;
function localize(position) {
        $.getJSON(conf.dita_server + 'localize?lat=' + position.coords.latitude + "&lon="+ position.coords.longitude+"&user=" + JSON.parse(window.sessionStorage.getItem("user")).email, function (data, status) {});
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
        }, LOCALIZE_EVERY)
}




$(document).ready(function(){
    $("#popup").hide();
    var pois = JSON.parse(window.sessionStorage.getItem("pois"));
    console.log(pois);
    var markers = {};//new Markers();

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

                var info = "<h3>"+format_name(x.display_name)+"<a  target=\"_top\" href=\"visit.html?type="+type+"&num="+i+"\">Visit</a></h3>" +
                    "<span onclick='$(\"#popup\").hide()' class='ui-btn-icon-right ui-icon-close'></span>" +
                    "<span style='color:lightsteelblue'>"+format_name_from(x.display_name)+":"+x.place_id+"</span>";



                var icon = markerIcons[type];
                if(!icon) icon = markerIcons["attractions"];


                var marker = L.marker([lat, lon], {icon: icon, mypopup:info});
                //marker.bindPopup("<h2>"+format_name(x.display_name)+"</h2><br><span style='color:lightsteelblue'>"+format_name_from(x.display_name)+":"+x.place_id+"</span><br><a  target=\"_top\" href=\"visit.html?type="+type+"&num="+i+"\">Visit</a>").openPopup().addTo(markers[type]);

                marker.on('click',function(e) {
                    //console.log(e.target.options.mypopup)
                    $("#popup").html(e.target.options.mypopup);
                    $("#popup").show()
                });

                if(!markers[type]) markers[type]  = new L.LayerGroup();
                marker.addTo(markers[type]);

                minLat = Math.min(minLat, lat);
                minLon = Math.min(minLon, lon);
                maxLat = Math.max(maxLat, lat);
                maxLon = Math.max(maxLon, lon)
            }
    }





    var layers = [];
    for(k in markers)
        layers.push(markers[k])

    mymap = L.map('mapid',{
        attributionControl: false,
        scrollWheelZoom: true,
        doubleClickZoom: true,
        zoomControl: true,
        touchZoom: true,
        dragging: true,
        layers: layers
    });




    $.when(translateObjKeys(markers)).done(function(){L.control.layers(null,markers).addTo(mymap)});


    L.easyButton('fa-crosshairs fa-lg', function(btn, map) {
        dragged = false;
    }).addTo(mymap);



    mymap.on('drag', function(e) {
        dragged = true;
    });


    mymap.fitBounds([
        [minLat, minLon],
        [maxLat, maxLon]
    ]);

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png").addTo(mymap);


    if(conf.localize)
        if(navigator.geolocation)
            navigator.geolocation.getCurrentPosition(localize);
        else
            alert("Turn on GPS");

    $("#itinerary").click(function(){
        var data = JSON.parse(window.sessionStorage.getItem("itineraries"));
        if(data == null)
            window.location.href = "itinerary_create.html";
        else
            window.location.href = "itineraries.html";
    })

});