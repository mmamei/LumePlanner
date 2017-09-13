var mymap;

var tdistance = 50; // 50 meters tolerance
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

    $("#mapid").hide();

    mymap = L.map('mapid',{
        attributionControl: false,
        scrollWheelZoom: true,
        doubleClickZoom: true,
        zoomControl: true,
        touchZoom: true,
        dragging: true,
    });

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

            $("#mapid").show();

            var str = "<ul>";
            for(var k in data.itineraries) {

                str += "<li>" + k + "--> " +checkIti(k,data)+ "</li>"
            }
            str += "</ul>";


            $("#result").html(str)
        })
    });

    $("#home").click(function() {
        window.location = "index.html"
    })

});