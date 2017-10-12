
var all_cities = null;

$.getJSON(conf.dita_server+"cities",function(data, status){
    all_cities = data
});



function sort_on_distance(position) {

    if(all_cities == null) {
        setTimeout(sort_on_distance(position),100);
        return;
    }
    var lat = position.coords.latitude;
    var lng = position.coords.longitude;
    window.sessionStorage.setItem("prevLat", lat);
    window.sessionStorage.setItem("prevLon", lng);
    all_cities.sort(function (a, b) {

        var lata = (a.lonLatBBox[1]+a.lonLatBBox[3]) / 2;
        var lnga = (a.lonLatBBox[0]+a.lonLatBBox[2]) / 2;

        var latb = (b.lonLatBBox[1]+b.lonLatBBox[3]) / 2;
        var lngb = (b.lonLatBBox[0]+b.lonLatBBox[2]) / 2;

        var da = getDistanceFromLatLonInM(lat, lng, lata, lnga);
        var db = getDistanceFromLatLonInM(lat, lng, latb, lngb);
        if (da < db) return -1;
        if (da > db) return 1;
        return 0;
    });
    init()
}

if (conf.localize && navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(sort_on_distance)
}


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

function loadPreferences(user) {
    console.log("get preferences for "+user);

    return $.postJSON(conf.dita_server + "loadpref", user, function (data, status) {
        console.log(data);
        window.sessionStorage.setItem("preferences",JSON.stringify(data));
    });
}


function init(str) {

    if(all_cities == null) {
        setTimeout(init(position,str),100);
        return;
    }

    if(!str) str = "";
    str = str.toLowerCase();
    var data = [];
    for(var i=0; i<all_cities.length;i++)
        if(all_cities[i].pretty_name.toLowerCase().startsWith(str))
            data.push(all_cities[i]);


    data = data.slice(0,3);
    $("#cities").html("");
    console.log("Selected cities");
    for(var i=0; i<data.length;i++) {
        console.log(data[i]);
        $("#cities").append(formatCityBlock(data[i].name,data[i].pretty_name, encodeURI(conf.dita_server_img+"cities/"+data[i].imgFile)));
    }





    $("img").click(function(event) {
        console.log(event);
        $(this).css("opacity","0.5");
        console.log("the user selected "+event.target.id);
        // update city
        if(window.sessionStorage.getItem("city") !== event.target.id) {
            console.log("update city");

            var city = event.target.id;



            var citybbox = null;
            for(var i=0; i<data.length;i++)
                if(data[i].name == city)
                    citybbox = data[i].lonLatBBox;

            window.sessionStorage.setItem("city", city);
            window.sessionStorage.setItem("citybbox", JSON.stringify(citybbox));
            //alert(city)
            //alert(JSON.stringify(cityBbox[city]))
            $.when(loadActivities(city,user)).done(function(){
                go2Map()
            });
        }
        else {
            go2Map()
        }
    });
}

function go2Map() {
    if(window.sessionStorage.getItem("preferences") == null) {
        setTimeout(go2Map, 100);
        return;
    }
    window.location.href = "map.html";
}


function checkAlertPopup(){
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
    } catch (e) {
        console.log("cordova.plugins.diagnostic not available")
    }


    $(document).ready(function () {

        //checkAlertPopup()
        init();

        // login user
        user = window.localStorage.getItem("user");
        if (!user) {
            console.log("user must be created");
            user = ("" + Math.random()).substring(2);
            window.localStorage.setItem("user",user)
        }
        loadPreferences(user);

        $("#search").keyup(function() {
            init($("#search").val())
        })

    });
}
document.addEventListener("deviceready", onDeviceReady, false);