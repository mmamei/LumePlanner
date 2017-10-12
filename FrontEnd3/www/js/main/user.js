



$(document).ready(function () {
    $.postJSON(conf.dita_server + "loadpref", window.localStorage.getItem("user"), function (data, status) {
        var prefs = data;
        window.sessionStorage.setItem("preferences",JSON.stringify(prefs));
        var size = 0;
        for(var k in prefs) size++


        for(var pref in prefs)
            $("#preferences").append(formatUserPrefBox(pref,prefs[pref]))


        $(".ui-btn-icon-notext").click( function() {

            var x = $(this).attr("id").split("_");
            var sign = x[0];
            var pref = x[1];
            var delta = 0;
            if (sign == "plus") delta = 0.05;
            if (sign == "minus") delta = -0.05;

            for (var k in prefs) {
                if (k == pref) {
                    prefs[k] += delta;
                    $("#bar_" + k).css("width", 100 * prefs[k] + "%")
                }
                else {
                    //console.log(delta / (size - 1))
                    prefs[k] -= (delta / (size - 1));
                    $("#bar_" + k).css("width", 100 * prefs[k] + "%")
                }
                //console.log(k+" ==> "+prefs[k])
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