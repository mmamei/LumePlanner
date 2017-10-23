
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
    //L.tileLayer.provider('OpenStreetMap.BlackAndWhite').addTo(mymap);
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png").addTo(mymap);


    //mymap.fitBounds([
    //    [cityLonLatBbox[1], cityLonLatBbox[0]],
    //    [cityLonLatBbox[3], cityLonLatBbox[2]]
    //]);

    L.easyButton('fa-crosshairs fa-lg', function(btn, map) {
        dragged = false;
    }).addTo(mymap);


    mymap.on('zoom', function(e) {
        console.log("call selectMarkers from zoom");
        selectMarkers()
    });

    mymap.on('drag', function(e) {
        dragged = true;
        console.log("call selectMarkers from drag");
        selectMarkers();
    });

    if(map_type == MAP_TYPES.NEXT_STEP) setupDestination();

    document.addEventListener('deviceready', onDeviceReady, false);
        function onDeviceReady() {
            //alert("xxx")
            if (conf.localize) {
                // BackgroundGeolocation is highly configurable. See platform specific configuration options
                backgroundGeolocation.configure(
                    function (location) {
                        //console.log('[js] BackgroundGeolocation callback:  ' + location.latitude + ',' + location.longitude);
                        localize({
                            coords: {
                                latitude: location.latitude,
                                longitude: location.longitude,
                                accuracy: 1
                            }
                        });
                        backgroundGeolocation.finish();
                    }, function (error) {
                        console.log('BackgroundGeolocation error')
                    }, {
                        desiredAccuracy: 10,
                        stationaryRadius: 20,
                        distanceFilter: 20,
                        interval: 10000
                    });
                // Turn ON the background-geolocation system.  The user will be tracked whenever they suspend the app.
                //alert("start")
                backgroundGeolocation.start();
            }
    }



    if(conf.localize && navigator.geolocation)
        navigator.geolocation.watchPosition(localize,function(){console.log("error")},{enableHighAccuracy: true, maximumAge: 5000});
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
        getBusInfo(currentDestination)
    })

});

var prev_path_coords;
var timed_update;
function simulatedMovement() {
    prevLat = (cityLonLatBbox[1] + cityLonLatBbox[3]) / 2;
    prevLon = (cityLonLatBbox[0] + cityLonLatBbox[2]) / 2;
    var t = 0;
    timed_update = setInterval(function() {
        if(path_coords2Itinerary == null || t >= path_coords2Itinerary.coordinates.length) {
            localize({
                coords: {
                    latitude: prevLat,
                    longitude: prevLon,
                    accuracy: 1
                }
            })
        }
        else {
            if(JSON.stringify(path_coords2Itinerary)!=JSON.stringify(prev_path_coords)) {
                t = 0;
                prev_path_coords = path_coords2Itinerary
            }
            localize({
                coords: {
                    latitude: path_coords2Itinerary.coordinates[t][1],
                    longitude: path_coords2Itinerary.coordinates[t][0],
                    accuracy: 1
                }
            });
            t++;
            //if (t == path_coords.coordinates.length) clearInterval(timed_update)
        }
    },2000)
}
