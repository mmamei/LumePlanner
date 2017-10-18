


var prefs = null;
$(document).ready(function () {
    $.postJSON(conf.dita_server + "loadpref", window.localStorage.getItem("user"), function (data, status) {
        prefs = data;
        window.sessionStorage.setItem("preferences",JSON.stringify(prefs));

        for(var pref in prefs)
            $("#preferences").append(formatUserPrefBox(pref,prefs[pref]))


        $(".ui-btn-icon-notext").click( function() {

            var x = $(this).attr("id").split("_");
            var sign = x[0];
            var pref = x[1];
            var delta = 0;
            if (sign == "plus") delta = 5;
            if (sign == "minus") delta = -5;

            prefs[pref] += delta;
            if(prefs[pref] < 0) prefs[pref] = 0;

            var sum = 0;
            for (var k in prefs)
                sum += prefs[k]

            prefs01 = {};
            for (var k in prefs) {
                prefs01[k] = sum == 0 ? 0 : prefs[k] / sum;
                $("#bar_" + k).css("width", 100 * prefs01[k] + "%")
            }
        })
    });


    $("#save_pref").click(function(){

        var userpreference = {
            user: window.localStorage.getItem("user"),
            prefs:prefs
        };

        console.log(userpreference);

        $.postJSON(conf.dita_server + "updatepref", userpreference, function (data, status) {
            console.log("saved");
            window.sessionStorage.setItem("preferences",JSON.stringify(data));
        });
    })

});