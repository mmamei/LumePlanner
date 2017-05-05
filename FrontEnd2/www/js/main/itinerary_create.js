
function poiName2Obj(name,array) {

    for(var i=0; i<array.length;i++) {
        if(name == array[i].display_name.split(',')[0])
            return array[i];
    }
}
/*
 function poiNames2Obj(sarray,oarray) {
 var x = [];
 for(var i=0; i<sarray.length;i++) {
 x.push(poiName2Obj(sarry[i]),oarray)
 }
 }
 */

$(document).ready(function() {
    var city = window.sessionStorage.getItem("city");
    //$("#title").html("Plan your visit in "+city);



    var time = window.sessionStorage.getItem("time");

    if(!time) time = new Date();
    //$("#date-value1-1").val($.format.date(time, 'yyyy/MM/dd HH:mm:ss'));


    var pois  = JSON.parse(window.sessionStorage.getItem("pois"));
    var spois  = JSON.parse(window.sessionStorage.getItem("spois"));
    var itineraries  = JSON.parse(window.sessionStorage.getItem("itineraries"));


    console.log(pois);





    console.log("selected attractions = "+spois.attractions.length);


    console.log("get activties form cache");
    var departure = window.sessionStorage.getItem("departure");
    var arrival = window.sessionStorage.getItem("arrival");

    if(departure == null) {
        departure =  "Current Location";
        window.sessionStorage.setItem("departure", departure);
    }

    if(arrival == null) {
        arrival =  "Current Location";
        window.sessionStorage.setItem("arrival", arrival);
    }


    var curr_loc = "Current Location";
    $("#departure").append("<option "+(curr_loc === departure ? "selected='selected'" : "")+" value='" + curr_loc + "'>" + curr_loc + "</option>");
    $("#arrival").append("<option "+(curr_loc === departure ? "selected='selected'" : "")+" value='" + curr_loc + "'>" + curr_loc + "</option>");
    var i;
    for (i = 0; i < pois.hotels.length; i++) {
        //console.log(hotels[i])
        var name = pois.hotels[i].display_name.split(',')[0];
        $("#departure").append("<option "+(name === departure ? "selected='selected'" : "")+" value='" + name + "'>" + name + "</option>");
        $("#arrival").append("<option "+(name === arrival ? "selected='selected'" : "")+" value='" + name + "'>" + name + "</option>");
    }


    if(itineraries == null)
        $("#itineraries").hide();

    else {
        $("#select-itineraries").append("<option value=''>"+itineraries.length+" Itineraries Avaialble</option>");
        for (var i = 0; i < itineraries.length; i++)
            $("#select-itineraries").append("<option value='" + itineraries[i].itinerary_id + "'>" + itineraries[i].display_name + "</option>");
    }




    $("#departure").change(function() {
        var v = $(this).val();
        //console.log(v)
        $("#arrival option[value='"+v+"']").attr('selected','selected');
        window.sessionStorage.setItem("departure",v);
        window.sessionStorage.setItem("arrival",v)
    });

    $("#arrival").change(function() {
        var v = $(this).val();
        window.sessionStorage.setItem("arrival",v)
    });


    $("#select-itineraries").change(function() {
        var v = $(this).val();
        for(var i=0; i<itineraries.length;i++)
            if (itineraries[i].itinerary_id == v) {
                for(var j=0; j<itineraries[i].visits.length;j++)
                    spois.attractions.push({place_id:itineraries[i].visits[j]})
                //console.log(spois)
                window.sessionStorage.setItem("spois", JSON.stringify(spois))
            }
        console.log(spois)
    });


    var slid_val = 1.0;
    $(".crowdb").click(function() {
        var id = $(this).attr("id");

        if(id === "crowd-fe"){
            $("#crowd-fe").css("background-color", "LightGreen");
            $("#crowd-me").css("background-color", "white");
            $("#crowd-mc").css("background-color", "white");
            $("#crowd-fc").css("background-color", "white");
            slid_val = 1.0
        }
        else  if(id === "crowd-me") {
            $("#crowd-fe").css("background-color", "green");
            $("#crowd-me").css("background-color", "green");
            $("#crowd-mc").css("background-color", "white");
            $("#crowd-fc").css("background-color", "white");
            slid_val = 0.5
        }
        else  if(id === "crowd-mc"){
            $("#crowd-fe").css("background-color", "orange");
            $("#crowd-me").css("background-color", "orange");
            $("#crowd-mc").css("background-color", "orange");
            $("#crowd-fc").css("background-color", "white");
            slid_val = -0.5
        }
        else  if(id === "crowd-fc"){
            $("#crowd-fe").css("background-color", "DarkRed");
            $("#crowd-me").css("background-color", "DarkRed");
            $("#crowd-mc").css("background-color", "DarkRed");
            $("#crowd-fc").css("background-color", "DarkRed");
            slid_val = -1.0
        }


    });

    $(".activities").click(function(){
        var id = $(this).attr("id");
        window.sessionStorage.setItem("sel", id);
        window.location.href = "attractions.html";
    });

    $("#go").click(function(){

        var dep = window.sessionStorage.getItem("departure");
        var arr = window.sessionStorage.getItem("arrival");

        if(dep === "Current Location" || arr === "Current Location") {
            navigator.geolocation.getCurrentPosition(function(position) {
                submit(position.coords.latitude,position.coords.longitude);
                //submit(44.695246, 10.629712)
            });
        }
        else submit()

    });

    function submit(lat, lng) {
        var start_place;
        var dep = window.sessionStorage.getItem("departure");
        if(dep === "Current Location") {
            console.log("current location");

            start_place = {
                display_name: "0",
                place_id: "0",
                lat: lat,
                lon: lng
            };
        }
        else {
            var start_obj = poiName2Obj(dep, pois.hotels);
            start_place = {
                display_name: start_obj.display_name,
                place_id: start_obj.place_id,
                lat: start_obj.geometry.coordinates[1],
                lon: start_obj.geometry.coordinates[0]
            };
        }

        var end_place;
        var arr = window.sessionStorage.getItem("arrival");
        if(arr === "Current Location") {
            console.log("current location");
            end_place = {
                display_name: "0",
                place_id: "0",
                lat: lat,
                lon: lng
            };
        }
        else {
            var end_obj = poiName2Obj(arr, pois.hotels);
            end_place = {
                display_name: end_obj.display_name,
                place_id: end_obj.place_id,
                lat: end_obj.geometry.coordinates[1],
                lon: end_obj.geometry.coordinates[0]
            };
        }
        //console.log(JSON.stringify(start_place))
        //console.log(JSON.stringify(end_place))


        var visits = [];



        for(k in spois) {
            for(var i=0; i<spois[k].length;i++)
                visits.push(spois[k][i].place_id)
        }


        var request = {
            user :  JSON.parse(window.sessionStorage.getItem("user")).email,
            city: city,
            crowd_preference : slid_val,
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
});