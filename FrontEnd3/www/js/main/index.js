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
        if(position && position.coords) {
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
        else {
            conf.localize = false;
        }

        if(!showall)
            data = data.slice(0,3);
        $("#cities").html("");
        for(var i=0; i<data.length;i++) {
            console.log("-----"+data[i]);
            var city = data[i].split(",")[0];
            var img = conf.dita_server_img+"cities/"+city+".jpg";
            $("#cities").append(formatCityBlock(city,img));
        }


        $.getJSON(conf.dita_server_files+'alert.json', function (alertdata, status) {
            if(alertdata && alertdata.id !== window.localStorage.getItem("last_seen_alert")) {
                //console.log(alertdata)
                var content = alertdata.it;
                if(langCode == "en") content = alertdata.en;
                //console.log(langCode)
                $("#infoPopup").html(content);
                $("#infoPopup").popup("open");
                window.localStorage.setItem("last_seen_alert",alertdata.id);
            }

        });


        $("img").click(function(event) {

            $(this).css("opacity","0.5");

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
function onDeviceReady() {

    try {
        cordova.plugins.diagnostic.isLocationAvailable(function (available) {
            if (available) conf.localize = true;
            if (!available) {
                alert("Attiva la localizzazione per utilizzare l'applicazione al meglio");
                cordova.plugins.diagnostic.switchToLocationSettings();
            }
        }, function (error) {
            console.log("The following error occurred: " + error);
        });
    } catch(e) {
        console.log("cordova.plugins.diagnostic not available")
    }
    $(document).ready(function () {
        if (conf.localize && navigator.geolocation)
            navigator.geolocation.getCurrentPosition(init, init,{timeout:5000});
        else init();

        $("#more").click(function (event) {
            showall = true;
            $("#cities").html("");
            if (conf.localize && navigator.geolocation) navigator.geolocation.getCurrentPosition(init, init,{timeout:5000});
            else init()

        });


        // login user
        user = window.localStorage.getItem("user");
        if (!user) {
            console.log("user must be created");
            var r = "" + Math.random();
            user = {id: "", email: r, password: r};
            console.log(user);
            $.postJSON(conf.dita_server + "signup", user,
                function (data, status) {
                    console.log("Data: " + data + "\nStatus: " + status);
                    if (data === "true" || data === true) {
                        console.log("registration succedded");
                        window.localStorage.setItem("user", JSON.stringify(user))
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
}
document.addEventListener("deviceready", onDeviceReady, false);