var mymap;

var tdistance = 100; // 100 meters tolerance

var userid = JSON.parse(window.localStorage.getItem("user")).email.substring(2);

function check() {
    var usr = $("#user").val();
    console.log("check "+usr);
    $.getJSON(conf.dita_server + 'checkuser?userid=' + usr, function (data, status) {
        console.log(data);

        if(data.latLon.length > 1) {
            var polyline = L.polyline(data.latLon, {color: 'red'}).addTo(mymap);

            for (var k in data.itineraries) {
                var pois = data.itineraries[k];

                for (var i = 0; i < pois.length; i++) {
                    var lonlatp = pois[i].geometry.coordinates;
                    L.marker([lonlatp[1], lonlatp[0]]).addTo(mymap).bindPopup(format_name(pois[i].display_name));
                }
            }

            mymap.fitBounds(polyline.getBounds());
        }
        var str = "";
        if(userid != usr) {
            if (data.gotPrize)
                str += "<div id='prize' style='background-color: gold; margin: 0 auto' class='ui-btn ui-shadow ui-corner-all ui-icon-star ui-btn-icon-notext ui-btn-b'></div>";
            else
                str += "<div id='prize' style='background-color: gray; margin: 0 auto' class='ui-btn ui-shadow ui-corner-all ui-icon-star ui-btn-icon-notext ui-btn-b'></div>";
        }


        for(var k in data.itineraries) {
            str += "<div class='itiner-box ui-corner-all ui-mini'>&nbsp;<span class='itiner'>"+k+"</span>&nbsp;";
            var check = checkIti(k,data);
            console.log(check);
            for(var j=0; j<check.length;j++)
                if(check[j][0]) str += "<span lat='"+check[j][1]+"' lng='"+check[j][2]+"' class='checkpoint ui-btn ui-shadow ui-corner-all ui-icon-check ui-btn-icon-notext ui-btn-b ui-btn-inline ui-mini'></span>";
                else str += "<span lat='"+check[j][1]+"' lng='"+check[j][2]+"' class='checkpoint ui-btn ui-shadow ui-corner-all ui-icon-delete ui-btn-icon-notext ui-btn-b ui-btn-inline ui-mini'></span>";
            str += "</div>"
        }



        $("#result").html(str);

        $(".checkpoint").click(function() {
            //console.log("check at "+$(this).attr("lat")+","+$(this).attr("lng"))
            mymap.setView([$(this).attr("lat"),$(this).attr("lng")],18);
        });

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
                $("#prize").css("background-color","gold")
            })
        })
    })
}



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
        pois_visited.push([visited,lonlatp[1],lonlatp[0]])
    }
    return pois_visited
}


$(document).ready(function(){

    $("#user").val(userid);
    check();
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
       check()
    });

    $("#home").click(function() {
        window.location = "index.html"
    })

});