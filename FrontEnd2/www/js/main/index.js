function loadActivities(city) {
    console.log("get activities for city "+city+" from server");
    return  $.getJSON(conf.dita_server + 'activities?city=' + city,
        function (data, status) {
            var pois = new POIS();
            //console.log("Data: " + data + "\nStatus: " + status);
            data.forEach(function (value) {
                switch (value.category) {
                    case "attractions":
                        pois.attractions.push(value);
                        break;
                    case "monuments" :
                        pois.monuments.push(value);
                        break;
                    case "museums" :
                        pois.museums.push(value);
                        break;
                    case "eating" :
                        pois.restaurants.push(value);
                        break;
                    case "parks" :
                        pois.parks.push(value);
                        break;
                    case "resting" :
                        pois.hotels.push(value);
                        break;
                    case "historical_sites" :
                        pois.historical.push(value);
                        break;
                    case "religious_sites" :
                        pois.religious.push(value);
                        break;
                    default :
                        console.log("Invalid category:" + value.category);
                }
            });
            window.sessionStorage.setItem("pois",JSON.stringify(pois));
            var departure = window.sessionStorage.getItem("departure");
            var arrival = window.sessionStorage.getItem("arrival");
        });
}

function loadItineraries(city) {
    window.sessionStorage.setItem("itineraries",null);
    console.log("get itineraries for city "+city+" from server");
    return  $.getJSON(conf.dita_server + 'itineraries?city=' + city,
        function (data, status) {
            if(data.length > 0)
                window.sessionStorage.setItem("itineraries",JSON.stringify(data));
        });
}


$(document).ready(function(){

    $.getJSON(conf.dita_server+"cities",function(data, status){
        for(var i=0; i<data.length;i++) {
            //$("#cities").append("<div class=\"container-fluid text-center\"><h2>"+data[i]+"</h2><img src=\""+conf.dita_server_img+"cities/"+data[i]+".jpg\" class=\"img-responsive img-rounded\" alt=\""+data[i]+"\" id=\""+data[i]+"\"></div>");
            $("#cities").append("<div class=\"ui-block-a\"><h3 align = \"center\">"+data[i]+"</h3><img src=\""+conf.dita_server_img+"cities/"+data[i]+".jpg\" class=\"img-responsive img-rounded\" alt=\""+data[i]+"\" id=\""+data[i]+"\"></div>");
        }




        $("img").click(function(event) {

            console.log("the user selected "+event.target.id);


            // update city
            if(window.sessionStorage.getItem("city") !== event.target.id) {
                console.log("update city");

                var city = event.target.id;
                window.sessionStorage.setItem("city", event.target.id);
                window.sessionStorage.setItem("spois",JSON.stringify(new POIS()));
                $.when(loadActivities(city), loadItineraries(city)).done(function(){
                    window.location.href = "map.html";
                });
            }
            else {
                window.location.href = "map.html";
            }
        });


    });

    // login user
    var user = window.sessionStorage.getItem("user");
    if(!user) {
        console.log("user must be created");
        var r = ""+Math.random();
        user = {id: "", email: r, password: r};
        console.log(user);
        $.postJSON(conf.dita_server+"signup",user,
            function(data, status){
                console.log("Data: " + data + "\nStatus: " + status);
                if (data === "true" || data === true) {
                    console.log("registration succedded");
                    window.sessionStorage.setItem("user",JSON.stringify(user))
                }
                else console.log("registration failed");

            });
    }
    else {
        user = JSON.parse(user);
    }
    // login was used only to retrieve an existing unfinished plan.
    // for now, we just remove it

    //console.log("user: "+window.localStorage.getItem("hashed_user"));


});