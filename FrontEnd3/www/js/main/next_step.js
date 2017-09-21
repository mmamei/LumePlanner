

var visitplan;
var type_of_plan;
var items;

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

    if(next_step) setupDestination();

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

    if(next_step)
    mymap.setView([prevLat,prevLon], 18);


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