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

    for(k in spois) {
        for(var i=0; i<spois[k].length;i++)
            visits.push(spois[k][i].place_id)
    }


    var request = {
        user :  JSON.parse(window.sessionStorage.getItem("user")).email,
        city: window.sessionStorage.getItem("city"),
        start_time : "09:00", //new Date($("#date-value1-1").val()).getHours(),
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


$(document).ready(function(){

    var data;


    data = JSON.parse(window.sessionStorage.getItem("itineraries"));
    console.log(data);

    if(data != null) {
        for (var i = 0; i < data.length; i++) {
            var name = data[i].display_name + "," + data[i].approx_time;
            $("#itineraries").append(formatButton(i,name));
        }
    }

    $(".itiner").click(function(){
        var spois  = JSON.parse(window.sessionStorage.getItem("spois"));
        var i = $(this).attr("num");

        for(var j=0; j<data[i].visits.length;j++)
            spois.attractions.push({place_id:data[i].visits[j]})
        //console.log(spois)
        window.sessionStorage.setItem("spois", JSON.stringify(spois));
        navigator.geolocation.getCurrentPosition(function(position) {
            submit(position.coords.latitude,position.coords.longitude,spois);
            //submit(44.695246, 10.629712)
        });
    });



    $("#custom").click(function(){
        window.location.href = "itinerary_create.html";
    })


});