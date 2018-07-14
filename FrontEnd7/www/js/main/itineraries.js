/**
 * Created by marco on 02/05/2017.
 */


function submit(lat, lng, spois) {

    var start_place = {
            display_name: "0",
            place_id: "0",
            lat: lat,
            lon: lng
    };


    var end_place = {
            display_name: "0",
            place_id: "0",
            lat: lat,
            lon: lng
    };


    var visits = [];

    for(var i=0; i<spois.length;i++) {
            visits.push(spois[i].place_id)
    }


    var request = {
        user :  window.localStorage.getItem("user"),
        city: window.sessionStorage.getItem("city"),
        start_time : $.format.date(new Date(), 'yyyy/MM/dd HH:mm:ss'),
        visits : visits,
        start_place: start_place,
        end_place : end_place,
    };

    console.log(JSON.stringify(request));

    $.postJSON(conf.dita_server+"newplan",request,function(data, status){
        window.sessionStorage.setItem("visitplan",JSON.stringify(data));
        window.location.href = "plan.html"
    });

}


var city = window.sessionStorage.getItem("city");
var user = window.localStorage.getItem("user");
var lat = window.sessionStorage.getItem("lat");
var lng = window.sessionStorage.getItem("lng");
var cityLonLatBbox = JSON.parse(window.sessionStorage.getItem("citybbox"));
var inCity = cityLonLatBbox[0] <= lng && lng <= cityLonLatBbox[2] &&
             cityLonLatBbox[1] <= lat && lat <= cityLonLatBbox[3];

$(document).ready(function(){

    if(!inCity) {
        lat = (cityLonLatBbox[1] + cityLonLatBbox[3]) / 2;
        lng = (cityLonLatBbox[0] + cityLonLatBbox[2]) / 2;
    }


    console.log("get itineraries for city "+city+" from server");
    console.log('itineraries?city=' + city + "&user="+user+"&lat="+lat+"&lng="+lng);

    // GET STANDARD ITINERARIES
    $.getJSON(conf.dita_server + 'itineraries?city=' + city + "&user="+user+"&lat="+lat+"&lng="+lng,
        function (data, status) {
            if (data.length > 0) {
                for (var i = 0; i < data.length; i++) {
                    var name = data[i].display_name;
                    var time = data[i].approx_time;
                    var img = data[i].img ? conf.dita_server_img + "itineraries/" + data[i].img : null;
                    $("#itineraries").append(formatButton(i, name, time, img, data[i].description));
                }
                translate();
                $("#itineraries").trigger('create');
            }

            $(".itiner").click(function(){
                $(this).css("opacity","0.5");
                var spois  = [];

                var i = $(this).attr("num");

                for(var j=0; j<data[i].visits.length;j++)
                    spois.push({place_id:data[i].visits[j]})
                //console.log(spois)
                window.sessionStorage.setItem("spois", JSON.stringify(spois));


                if(conf.localize)
                    if(navigator.geolocation) {

                        navigator.geolocation.getCurrentPosition(function (position) {

                            submit(position.coords.latitude,position.coords.longitude,spois);

                        });
                    }


                if(!conf.localize) {
                    var pois = JSON.parse(window.sessionStorage.getItem("pois"));
                    //console.log(pois)
                    //console.log(spois)
                    for(k in pois) {
                        for(var i=0; i<pois[k].length;i++)
                            if(pois[k][i].place_id == spois[0].place_id) {
                                submit(pois[k][i].geometry.coordinates[1],pois[k][i].geometry.coordinates[0],spois);
                            }
                    }
                }
            });
    });

    // GET GPX ITINERARIES
    $.getJSON(conf.dita_server_files + "gpx_itineraries/" + city + "/itineraries.json", function(data,status) {
        if(status == "error") return;
        if (data.length > 0) {
            for (var i = 0; i < data.length; i++) {
                var name = data[i].display_name;
                var time = data[i].approx_time;
                var img = data[i].img ? conf.dita_server_files + "gpx_itineraries/" + city + "/" + data[i].img : null;
                console.log(img);
                $("#itineraries").append(formatButtonGPX(i, name, time, img, data[i].description,"#a8c"));
            }
            translate();
            $("#itineraries").trigger('create');
        }
        $(".itinergpx").click(function(){
            $(this).css("opacity","0.5");
            var i = $(this).attr("num");
            window.sessionStorage.setItem("gpx",data[i].gpx);
            window.location.href = "map.html"
        })

    });

    // GET GPX ITINERARIES
    $.getJSON(conf.dita_server_files + "gpx_itineraries/" + city + "/itineraries_disabili.json", function(data,status) {
        if(status == "error") return;
        if (data.length > 0) {
            for (var i = 0; i < data.length; i++) {
                var name = data[i].display_name;
                var time = data[i].approx_time;
                var img = data[i].img ? conf.dita_server_files + "gpx_itineraries/" + city + "/" + data[i].img : null;
                console.log(img);
                $("#itineraries").append(formatButtonGPX(i, name, time, img, data[i].description,"#6600ff"));
            }
            translate();
            $("#itineraries").trigger('create');
        }
        $(".itinergpx").click(function(){
            $(this).css("opacity","0.5");
            var i = $(this).attr("num");
            window.sessionStorage.setItem("gpx",data[i].gpx);
            window.location.href = "map.html"
        })

    });

    $("#custom").click(function(){
        window.location.href = "itinerary_create.html";
    })


});