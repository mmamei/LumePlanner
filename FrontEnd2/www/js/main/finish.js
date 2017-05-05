$(document).ready(function(){

    $("#restart").click(function() {

        var user = JSON.parse(window.sessionStorage.getItem("user"));
        var city = window.sessionStorage.getItem("city");

        var request = {
            user: user.email,
            email: user.email,
            city: city
        };

        $.postJSON(conf.dita_server + "finish", request, function (data, status) {
            console.log("User plan deleted:" + data);

            window.sessionStorage.setItem("spois",JSON.stringify(new POIS()));
            window.sessionStorage.removeItem("time");
            window.sessionStorage.removeItem("departure");
            window.sessionStorage.removeItem("arrival");
            window.sessionStorage.removeItem("visitplan");
            window.location.href = "itineraries.html";


        });
    });
});