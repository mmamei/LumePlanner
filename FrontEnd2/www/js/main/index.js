function loadActivities(city,user) {
    console.log("get activities for city "+city+" from server");
    return  $.getJSON(conf.dita_server + 'activities?city=' + city + "&user="+user,
        function (data, status) {
            var pois = {};
            //console.log("Data: " + data + "\nStatus: " + status);
            data.forEach(function (value) {
                if(!pois[value.category]) pois[value.category] = [];
                pois[value.category].push(value)
            });
            window.sessionStorage.setItem("pois",JSON.stringify(pois));
            var departure = window.sessionStorage.getItem("departure");
            var arrival = window.sessionStorage.getItem("arrival");
        });
}

function loadItineraries(city, user) {
    window.sessionStorage.setItem("itineraries",null);
    console.log("get itineraries for city "+city+" from server");
    return  $.getJSON(conf.dita_server + 'itineraries?city=' + city + "&user="+user,
        function (data, status) {
            if(data.length > 0)
                window.sessionStorage.setItem("itineraries",JSON.stringify(data));
        });
}


var showall = false;
function init(position) {
    //console.log("localized at " + position.coords.latitude + "," + position.coords.longitude);
    $.getJSON(conf.dita_server+"cities",function(data, status){

        if(position != null) {
            data.sort(function(a,b) {
                var pa = a.split(",");
                var pb = b.split(",");
                var da = getDistanceFromLatLonInM(position.coords.latitude, position.coords.longitude, pa[2],pa[1]);
                var db = getDistanceFromLatLonInM(position.coords.latitude, position.coords.longitude, pb[2],pb[1]);
                if(da < db) return -1;
                if(da > db) return 1;
                return 0;
            })
        }

        if(!showall)
            data = data.slice(0,3);

        for(var i=0; i<data.length;i++) {
            console.log("-----"+data[i]);
            var city = data[i].split(",")[0];
            var img = conf.dita_server_img+"cities/"+city+".jpg";
            $("#cities").append(formatCityBlock(city,img));
        }


        $("img").click(function(event) {

            console.log("the user selected "+event.target.id);
            // update city
            if(window.sessionStorage.getItem("city") !== event.target.id) {
                console.log("update city");

                var city = event.target.id;
                window.sessionStorage.setItem("city", event.target.id);
                $.when(loadActivities(city,user.email), loadItineraries(city,user.email)).done(function(){
                    window.location.href = "map.html";
                });
            }
            else {
                window.location.href = "map.html";
            }
        });
    });
}

var user;

$(document).ready(function(){

    console.log("-----");

    if(conf.localize && navigator.geolocation) navigator.geolocation.getCurrentPosition(init);
    else init(null);

        $("#more").click(function(event) {
            showall = true;
            $("#cities").html("");

            if(conf.localize && navigator.geolocation) navigator.geolocation.getCurrentPosition(init);
            else init(null)

        });


    // login user
    user = window.sessionStorage.getItem("user");
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