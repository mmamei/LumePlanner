
$(document).ready(function(){

    next_step = false;

    if(window.sessionStorage.getItem("visitplan"))
        next_step = true;

    if(next_step)
          $("#itinerary").hide();
    else {
          $("#visit").hide();
          $("#bus").hide();
          $("#quit").hide()
    }

    console.log("next_step = "+next_step);

    $("#popup").hide();
    $("#visit_popup").hide();




    mymap = L.map('mapid',{
        attributionControl: false,
        scrollWheelZoom: true,
        doubleClickZoom: true,
        zoomControl: true,
        touchZoom: true,
        dragging: true,
    });
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png").addTo(mymap);

    L.easyButton('fa-crosshairs fa-lg', function(btn, map) {
        dragged = false;
    }).addTo(mymap);

    if(!next_step) {
        selectMarkers(pois);
        if(!conf.localize)
            mymap.fitBounds([
                [minLat, minLon],
                [maxLat, maxLon]
            ]);
    }



    mymap.on('zoom', function(e) {
        selectMarkers(pois,mymap.getBounds())
    });



    mymap.on('drag', function(e) {
        dragged = true;
        selectMarkers(pois,mymap.getBounds());

        prevLat = mymap.getCenter().lat;
        prevLon = mymap.getCenter().lng;
        window.sessionStorage.setItem("prevLat",prevLat);
        window.sessionStorage.setItem("prevLon",prevLon);
    });

    if(next_step) {
        mymap.setView([prevLat, prevLon], 18);
        setupDestination();
    }

    if(conf.localize && navigator.geolocation)
        navigator.geolocation.watchPosition(localize);
    else {
        if(next_step) simulatedMovement();
        if(!next_step) {
            prevLat = mymap.getCenter().lat;
            prevLon = mymap.getCenter().lng;
            window.sessionStorage.setItem("prevLat",prevLat);
            window.sessionStorage.setItem("prevLon",prevLon);
        }
    }

    $("#itinerary").click(function(){
        $(this).css("opacity","0.5");
        var data = JSON.parse(window.sessionStorage.getItem("itineraries"));
        if(data == null)
            window.location.href = "itinerary_create.html";
        else
            window.location.href = "itineraries.html";
    });

    $("#visit").click(function(){
        $(this).css("opacity","0.5");
        if (items.to_visit.length > 0) {
           //window.location.href = "visit.html"
            visit()
        } else {
            window.location.href = "finish.html"
        }
    });

    $("#quit").click(function(){
        $(this).css("opacity","0.5");
        var x = confirm("Sei sicuro?");
        if(x) window.location.href = "finish.html"
    });

    $("#bus").click(function(){
        window.location.href = "http://travelplanner.cup2000.it";
    })

});