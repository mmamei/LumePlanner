
    $(document).ready(function(){

    $("#popup").hide();
    $("#visit_popup").hide();
    if(map_type == MAP_TYPES.NEXT_STEP)
          $("#itinerary").hide();
    if(map_type == MAP_TYPES.MAP) {
        $("#mapid").css("height","90%");
        $("#popup").css("bottom","60px");
        $("#visit").hide();
        $("#bus").hide();
        $("#quit").hide()
    }
    if(map_type == MAP_TYPES.CROWD) {
        $("#mapid").css("height","100%");
        $("#popup").css("bottom","60px");
        $("#itinerary").hide();
        $("#visit").hide();
        $("#bus").hide();
        $("#quit").hide()
    }

    mymap = L.map('mapid',{
        attributionControl: false,
        scrollWheelZoom: true,
        doubleClickZoom: true,
        zoomControl: true,
        touchZoom: true,
        dragging: true,
    });
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png").addTo(mymap);


    //mymap.fitBounds([
    //    [cityLonLatBbox[1], cityLonLatBbox[0]],
    //    [cityLonLatBbox[3], cityLonLatBbox[2]]
    //]);

    L.easyButton('fa-crosshairs fa-lg', function(btn, map) {
        dragged = false;
    }).addTo(mymap);


    mymap.on('zoom', function(e) {
        selectMarkers()
    });

    mymap.on('drag', function(e) {
        dragged = true;
        selectMarkers();
    });

    if(map_type == MAP_TYPES.NEXT_STEP) setupDestination();


    if(conf.localize && navigator.geolocation)
        navigator.geolocation.watchPosition(localize,localize,{enableHighAccuracy: true});
    else simulatedMovement();


    $("#itinerary").click(function(){
        $(this).css("opacity","0.5");
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

    $("#real_quit").click(function(){
        window.location.href = "finish.html"
    });

    $("#bus").click(function(){
        //window.location.href = "http://travelplanner.cup2000.it";

        var from_name = "Tua Posizione";
        var from_lat = window.sessionStorage.getItem("lat");
        var from_lng = window.sessionStorage.getItem("lng");

        var to_name = currentDestination.display_name.split(",")[0];
        var to_lat = currentDestination.geometry.coordinates[1];
        var to_lng = currentDestination.geometry.coordinates[0];

        tpricerca("visit_popup",from_name, from_lat,from_lng,to_name,to_lat,to_lng);
        $("#visit_popup").show()
    })

});

var prev_path_coords;
var timed_update;
function simulatedMovement() {
    prevLat = (cityLonLatBbox[1] + cityLonLatBbox[3]) / 2;
    prevLon = (cityLonLatBbox[0] + cityLonLatBbox[2]) / 2;
    var t = 0;
    timed_update = setInterval(function() {
        if(path_coords == null || t >= path_coords.coordinates.length) {
            localize({
                coords: {
                    latitude: prevLat,
                    longitude: prevLon,
                    accuracy: 1
                }
            })
        }
        else {
            if(JSON.stringify(path_coords)!=JSON.stringify(prev_path_coords)) {
                t = 0;
                prev_path_coords = path_coords
            }
            localize({
                coords: {
                    latitude: path_coords.coordinates[t][1],
                    longitude: path_coords.coordinates[t][0],
                    accuracy: 1
                }
            });
            t++;
            //if (t == path_coords.coordinates.length) clearInterval(timed_update)
        }
    },2000)
}
