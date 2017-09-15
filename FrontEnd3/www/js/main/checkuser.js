var mymap;

var tdistance = 100; // 100 meters tolerance
function checkIti(name,data) {
    var pois = data.itineraries[name];
    var pois_visited = [];
    for(var i=0; i<pois.length;i++) {

        var lonlatp = pois[i].geometry.coordinates;
        var visited = false;
        for(var j=0; j<data.latLon.length;j++) {
            var latlonp = data.latLon[j];
            var d = getDistanceFromLatLonInM(lonlatp[1],lonlatp[0],latlonp[0],latlonp[1]);
            if(d < tdistance) {
                visited = true;
                break;
            }
        }
        pois_visited.push(visited)
    }
    return pois_visited
}


$(document).ready(function(){

    //$("#mapid").hide();

    mymap = L.map('mapid',{
        attributionControl: false,
        scrollWheelZoom: true,
        doubleClickZoom: true,
        zoomControl: true,
        touchZoom: true,
        dragging: true
    });

    mymap.fitBounds([
        [43.855691, 9.243638],
        [45.278507, 12.815065]
    ]);



    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png").addTo(mymap);


    $("#check").click(function() {
        var usr = $("#user").val();
        console.log("check "+usr);
        $.getJSON(conf.dita_server + 'checkuser?userid=' + usr, function (data, status) {
            console.log(data);


            var polyline = L.polyline(data.latLon, {color: 'red'}).addTo(mymap);

            for(var k in data.itineraries) {
                var pois = data.itineraries[k];

                for(var i=0; i<pois.length;i++) {
                    var lonlatp = pois[i].geometry.coordinates;
                    L.marker([lonlatp[1], lonlatp[0]]).addTo(mymap).bindPopup(format_name(pois[i].display_name));
                }
            }

            mymap.fitBounds(polyline.getBounds());

            //$("#mapid").show();

            var str = "";
            for(var k in data.itineraries) {
                str += "<div class='itiner ui-corner-all ui-mini'>&nbsp;"+k+"&nbsp;";
                var check = checkIti(k,data);
                for(var j=0; j<check.length;j++)
                    if(check[j]) str += "<span class='ui-btn ui-shadow ui-corner-all ui-icon-check ui-btn-icon-notext ui-btn-b ui-btn-inline ui-mini'></span>";
                    else str += "<span class='ui-btn ui-shadow ui-corner-all ui-icon-delete ui-btn-icon-notext ui-btn-b ui-btn-inline ui-mini'></span>";
                str += "</div>"
            }
            if(!data.gotPrize)
            str += "<div id='prize' class='ui-corner-all'><span style='margin: 0 auto' class='ui-btn ui-shadow ui-corner-all ui-icon-star ui-btn-icon-notext ui-btn-b'></span></div>";




            $("#result").html(str);
            $(".itiner").click(function() {
                var txt = $(this).text().trim();
                var points = data.itineraries[txt];
                var minLat = 1000;
                var minLon = 1000;
                var maxLat = -1000;
                var maxLon = -1000;

                for(var i=0; i<points.length;i++) {

                    lon = points[i].geometry.coordinates[0];
                    lat = points[i].geometry.coordinates[1];
                    minLat = Math.min(minLat, lat);
                    minLon = Math.min(minLon, lon);
                    maxLat = Math.max(maxLat, lat);
                    maxLon = Math.max(maxLon, lon)
                }

                mymap.fitBounds([
                    [minLat, minLon],
                    [maxLat, maxLon]
                ]);
            });



            $("#prize").click(function () {
                $.getJSON(conf.dita_server + 'log?txt=user 0.'+usr+' got prize!', function (data, status) {
                    console.log("recorded");
                    $("#prize").hide()
                })
            })
        })
    });

    $("#home").click(function() {
        window.location = "index.html"
    })

});