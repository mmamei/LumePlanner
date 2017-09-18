


$(document).ready(function(){
    $("#popup").hide();

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

    if(!conf.localize)
        mymap.fitBounds([
            [minLat, minLon],
            [maxLat, maxLon]
        ]);
    L.easyButton('fa-crosshairs fa-lg', function(btn, map) {
        dragged = false;
    }).addTo(mymap);

    mymap.on('drag', function(e) {
        dragged = true;
        selectMarkers(pois,mymap.getBounds());

        prevLat = mymap.getCenter().lat;
        prevLon = mymap.getCenter().lng;
        window.sessionStorage.setItem("prevLat",prevLat);
        window.sessionStorage.setItem("prevLon",prevLon);

    });

    mymap.on('zoom', function(e) {
        dragged = true;
        selectMarkers(pois,mymap.getBounds())
    });


    if(conf.localize) {
        if (navigator.geolocation)
            navigator.geolocation.getCurrentPosition(localize);
        else
            alert("Turn on GPS");
    }
    else {
        prevLat = mymap.getCenter().lat;
        prevLon = mymap.getCenter().lng;
        window.sessionStorage.setItem("prevLat",prevLat);
        window.sessionStorage.setItem("prevLon",prevLon);
    }





    $("#itinerary").click(function(){
        $(this).css("opacity","0.5");
        var data = JSON.parse(window.sessionStorage.getItem("itineraries"));
        if(data == null)
            window.location.href = "itinerary_create.html";
        else
            window.location.href = "itineraries.html";
    });
});