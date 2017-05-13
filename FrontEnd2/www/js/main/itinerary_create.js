
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


var curr_loc = langCode === "en" ? "Current Location" : "Posizione Corrente";

$(document).ready(function() {
    var city = window.sessionStorage.getItem("city");
    $("#title").html("Plan your visit in "+city);

    var time = window.sessionStorage.getItem("time");
    if(!time) time = new Date();
    initTime(time);



    var pois  = JSON.parse(window.sessionStorage.getItem("pois"));



    var matr = [[]];
    var i = 0;
    for(var k in pois) {
        matr[matr.length-1].push(k);
        i++;
        if(i==3) {
            i=0;
            matr.push([])
        }
    }

    var letters = ["a","b","c"];
    var htmlbuttons = "";
    for(var i=0; i<matr.length;i++) {
        
        htmlbuttons = htmlbuttons.concat("<div class='ui-grid-b'>");
        
        for(var j=0; j<matr[i].length;j++) {

            htmlbuttons = htmlbuttons.concat("<div class='ui-block-"+letters[j]+"'>");
            htmlbuttons = htmlbuttons.concat(" <div id='"+matr[i][j]+"' class='attractions ui-input-btn ui-btn ui-corner-all ui-shadow ui-mini'><span tkey='"+matr[i][j]+"'>"+matr[i][j]+"</span>");
            htmlbuttons = htmlbuttons.concat("<input type='button' data-enhanced='true' value='Input value'>");
            htmlbuttons = htmlbuttons.concat("</div>");
            htmlbuttons = htmlbuttons.concat("</div>")

        }

        htmlbuttons = htmlbuttons.concat("</div>");
    }


    $("#attraction_buttons").html(htmlbuttons);
    translate();






    var spois  = JSON.parse(window.sessionStorage.getItem("spois"));
    if(!spois) {
        console.log("create spois");
        spois = []
    }

    //console.log(spois);
    console.log("selected attractions = "+spois.length);


    console.log("get activties form cache");
    var departure = window.sessionStorage.getItem("departure");
    var arrival = window.sessionStorage.getItem("arrival");

    if(departure == null) {
        departure =  curr_loc;
        window.sessionStorage.setItem("departure", departure);
    }

    if(arrival == null) {
        arrival =  curr_loc;
        window.sessionStorage.setItem("arrival", arrival);
    }



    $("#departure").append(formatDepartureArrival(curr_loc));
    $("#arrival").append(formatDepartureArrival(curr_loc));
    var i;
    for (i = 0; i < pois.resting.length; i++) {
        //console.log(hotels[i])
        var name = pois.resting[i].display_name.split(',')[0];
        $("#departure").append(formatDepartureArrival(name, name===departure));
        $("#arrival").append(formatDepartureArrival(name, name===arrival));
    }


    $("#departure").change(function() {
        var v = $(this).val();
        //console.log("----"+v)
        //console.log( $("#arrival option[value='"+v+"']").prop('selected'))
        $("#arrival option[value='"+v+"']").prop('selected', true);
        //console.log( $("#arrival option[value='"+v+"']").prop('selected'))


        window.sessionStorage.setItem("departure",v);
        window.sessionStorage.setItem("arrival",v)
    });

    $("#arrival").change(function() {
        var v = $(this).val();
        window.sessionStorage.setItem("arrival",v)
    });


    $(".attractions").click(function(){
        var id = $(this).attr("id");
        window.sessionStorage.setItem("sel", id);
        window.location.href = "attractions.html";
    });

    $("#go").click(function(){

        var dep = window.sessionStorage.getItem("departure");
        var arr = window.sessionStorage.getItem("arrival");

        if(dep === curr_loc || arr === curr_loc) {
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
        if(dep === curr_loc) {
            console.log(curr_loc);

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
        if(arr === curr_loc) {
            console.log(curr_loc);
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

        if(spois.length == 0)
            return;

        var visits = [];
        for(var i=0; i<spois.length;i++)
            visits.push(spois[i].place_id)

        var request = {
            user :  JSON.parse(window.sessionStorage.getItem("user")).email,
            city: city,
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