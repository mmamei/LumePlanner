$(document).ready(function(){


    function cleanup() {
        var user = JSON.parse(window.localStorage.getItem("user"));
        var city = window.sessionStorage.getItem("city");

        var request = {
            user: user.email,
            email: user.email,
            city: city
        };

        $.postJSON(conf.dita_server + "finish", request, function (data, status) {
            console.log("User plan deleted:" + data);
        });
        window.sessionStorage.setItem("spois",JSON.stringify([]));
        window.sessionStorage.removeItem("time");
        window.sessionStorage.removeItem("departure");
        window.sessionStorage.removeItem("arrival");
        window.sessionStorage.removeItem("visitplan");
    }

    $("#restart").click(function() {
        $(this).css("opacity","0.5");
        cleanup();
        window.location.href = "itineraries.html";
    });

    $("#map").click(function() {
        $(this).css("opacity","0.5");
        cleanup();
        window.location.href = "next_step.html";
    });

});