
$(document).ready(function(){
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


    $("#real_quit").click(function(){
        cleanupItinerary();
        window.location.href = "map.html"
    });

});


function visitItinerary() {
    if (items.to_visit.length > 0) {
        //window.location.href = "visit.html"
        visit()
    } else {
        // the itinerary is over!
        console.log(visitplan);
        var txt = "<h2 id='title' align='center'>Questo itinerario è terminato!</h2>";
        txt += "Hai visto:<br>";
        for(var i=0; i<visitplan.plans[type_of_plan].visited.length;i++)
            txt += "<span style='font-weight: bold;line-height:150%'>"+format_name(visitplan.plans[type_of_plan].visited[i].visit.display_name)+"</span><br>"

        txt += "<br><br>";
        txt += "<div id='share_btn' class='ui-btn ui-btn-b ui-shadow ui-corner-all ui-icon-carat-r ui-btn-icon-right ui-btn-active ui-state-persist'>Condividi con  <i id='fb' class='fa fa-facebook-f'></i>acebook </div><br>";
        txt += "<div onclick=\"window.location.href = 'map.html'\" class='ui-btn ui-btn-b ui-shadow ui-corner-all ui-icon-delete ui-btn-icon-right ui-btn-active ui-state-persist'>Chiudi</div>";
        $("#visit_popup").html(txt);
        $("#visit_popup").show();
        cleanupItinerary();

        $("#share_btn").click(function() {
            shareFB(visitplan.city)
        });
    }
}

var prev_path_coords;
var timed_update;
function simulatedMovement() {
    prevLat = (cityLonLatBbox[1] + cityLonLatBbox[3]) / 2;
    prevLon = (cityLonLatBbox[0] + cityLonLatBbox[2]) / 2;
    var t = 0;
    timed_update = setInterval(function() {
        var path_coords = path_coords2Clicked != null ? path_coords2Clicked :
                          path_coords2Itinerary != null ? path_coords2Itinerary : null;
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
