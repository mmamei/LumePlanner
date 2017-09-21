/**
 * Created by marco on 21/09/2017.
 */
function simulatedMovement() {


    var start = prevLat+","+prevLon;
    var end = currentDestination.place.geometry.coordinates[1] + "," + currentDestination.place.geometry.coordinates[0];

    console.log(start);
    console.log(end);



    /*
     //GH alt_key: LijBPDQGfu7Iiq80w3HzwB4RUDJbMbhs6BU0dEnn
     $.getJSON('https://graphhopper.com/api/1/route?' +
     'vehicle=foot&locale=en-US&key=e32cc4fb-5d06-4e90-98e2-3331765d5d77&instructions=false&points_encoded=false' +
     '&point=' + start + '&point=' + end, function (data, status) {
     console.log(data)
     })
     */



    $.getJSON(conf.dita_server + 'route?vehicle=foot&start=' + start + '&end=' + end, function (data, status) {
        //routing API
        console.log(data);
        var geoJsonPoints = data.points.coordinates;


        var distances = [];
        var d_tot = 0.0;
        var i, p_from, p_to, d;
        for (i = 0; i < geoJsonPoints.length - 1; i += 1) {
            p_from = geoJsonPoints[i];
            p_to = geoJsonPoints[i + 1];
            d = getDistanceFromLatLonInM(p_from[1], p_from[0], p_to[1], p_to[0]);
            distances.push(d);
            d_tot += d;
        }

        var d_rates = [];
        for (i = 0; i < distances.length; i += 1) {
            d_rates.push(distances[i] / d_tot);
        }

        var start_time = new Date().getTime();
        var timings = [];
        timings.push(start_time);
        for (i = 0; i < d_rates.length; i += 1) {
            start_time += parseInt(d_rates[i]* (1000 * data.length) , 10);
            timings.push(start_time);
        }


        var adaptedGeoJsonData = {
            type: "Feature",
            geometry: {
                type: "MultiPoint",
                coordinates: geoJsonPoints
            },
            properties: {
                time: timings
            }
        };
        console.log({adaptedGeoJson: adaptedGeoJsonData});

        var timed_update;
        var old_position;


        function playback(adaptedGeoJsonData) {

            // ***** This part place a placemark according to user simulated movement

            var options = {
                //tickLen : '', //millis [def:250]
                speed : 10.0, //float multiplier [def:1]
                tracksLayer : false,
                marker: {
                    icon: L.divIcon({
                        type: 'div',
                        className: 'marker',
                        html: "<span class=\"fa-col-blue\"><i class=\"fa fa-dot-circle-o fa-3x fa-rotate-dyn\"></i></span>"
                    })
                }
            };
            var v = new L.Playback(mymap, adaptedGeoJsonData, null, options);
            v.start();

            // ***** this part centers the map where is the previous placemerk

            timed_update = setInterval(function() {
                var latlng = mymap.layerPointToLatLng(mymap.getPanes().markerPane.childNodes[0]._leaflet_pos);
                //console.log(latlng)
                if (old_position && old_position===latlng) {
                    console.log("qui");
                }
                old_position = latlng;
                var dist =  getDistanceFromLatLonInM(parseFloat(end.split(',')[0]), parseFloat(end.split(',')[1]), latlng.lat, latlng.lng);
                $("#dist").html("mancano "+dist.toFixed(0)+" meters");
                //console.log(dist);
                if ( dist < 0) {
                    //changeView
                    clearInterval(timed_update);
                    if (items.to_visit.length > 0) {
                        window.location.href = "visit.html"
                    } else {
                        window.location.href = "finish.html"
                    }
                } else {
                    //console.log("moving:"+computeDistance(parseFloat(end.split(',')[0]), parseFloat(end.split(',')[1]), latlng.lat, latlng.lng));
                    mymap.panTo(L.latLng(latlng));
                    selectMarkers(pois,mymap.getBounds());
                    prevLat = mymap.getCenter().lat;
                    prevLon = mymap.getCenter().lng;
                    window.sessionStorage.setItem("prevLat",prevLat);
                    window.sessionStorage.setItem("prevLon",prevLon);
                }
            }, 1000);
        }


        playback(adaptedGeoJsonData);


        drawStartEndPlacemarks();

        var geojson = {
            data: data.points,
            style: {
                fillColor: "green",
                weight: 2,
                opacity: 1,
                color: 'blue',
                dashArray: '3',
                fillOpacity: 0.7
            }
        };

        L.geoJSON(geojson.data, {style: geojson.style}).addTo(mymap);

    });
}
