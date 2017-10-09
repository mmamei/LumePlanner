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
        user :  JSON.parse(window.localStorage.getItem("user")).email,
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
$(document).ready(function(){

    console.log("get itineraries for city "+city+" from server");
    $.getJSON(conf.dita_server + 'itineraries?city=' + city + "&user="+user.email+"&lat="+lat+"&lng="+lng,
        function (data, status) {
            if (data.length > 0) {
                for (var i = 0; i < data.length; i++) {
                    var name = data[i].display_name + "," + data[i].approx_time;
                    var img = data[i].img ? conf.dita_server_img + "itineraries/" + data[i].img : null;
                    $("#itineraries").append(formatButton(i, name, img, data[i].description));
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

    $("#custom").click(function(){
        window.location.href = "itinerary_create.html";
    })


});