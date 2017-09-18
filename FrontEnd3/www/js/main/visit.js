function find_pic (picture_query) {
    var q = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=9eab1c5e074fc0e1deb66c40d7535ad9&format=json&sort=relevance&page=1&per_page=1&nojsoncallback=1&text="+picture_query;
    console.log(q);
    $.get(q, function(data, status){
        console.log(data);
        if (data.photos.photo.length > 0) {
            var photo_url = data.photos.photo[0];
            var photo_url = "https://farm"+photo_url.farm+".staticflickr.com/"+photo_url.server+"/"+photo_url.id+"_"+photo_url.secret+"_b.jpg";
            console.log(photo_url);
            $("#photo_url").attr("src",photo_url);
            $("#photo_url").show();
        } else {
            console.log("not found:"+picture_query);
            if (picture_query.lastIndexOf(" ") !== -1) {
                picture_query = picture_query.substr(0, picture_query.lastIndexOf(" "));
                find_pic(picture_query);
            } else {
                console.log("picture not found");
            }
        }
    });
}



$(document).ready(function() {

    $("#photo_url").hide();
    $("#description").hide();
    $("#www").hide();

    console.log("------------------------------");
    console.log(window.location.href);

    var type = getUrlParameter("type");
    var id = getUrlParameter("id");
    var currentVisit = null;
    if(type && id) {
        var pois = JSON.parse(window.sessionStorage.getItem("pois"));
        for(var i = 0; i<pois[type].length;i++)
            if(pois[type][i].place_id == id) {
                currentVisit = pois[type][i];
                break;
            }
    }

    var currentDestination = JSON.parse(window.sessionStorage.getItem("currentDestination"));

    var actualVisit = {};

    // currentVisit è dove lui a cliccato
    // currentDestination è la porssima destinazione in itinerario

    if(currentDestination && !currentVisit) {
        actualVisit = currentDestination;
        $("#close").hide()
    }

    if(currentVisit) {
        actualVisit.place = currentVisit;

        //console.log(currentDestination.place.place_id)
        //console.log(currentVisit.place_id)
        if(!currentDestination || currentVisit.place_id != currentDestination.place.place_id)
            $("#next").hide();
        else
            $("#close").hide()
    }

    console.log(actualVisit);

    $.getJSON(conf.dita_server + 'look?poi=' + actualVisit.place.place_id + "&user=" + JSON.parse(window.localStorage.getItem("user")).email, function (data, status) {});


    $("#title").text(actualVisit.place.display_name.split(",")[0]);
    if(actualVisit.place.photo_url != null) {
        $("#photo_url").attr("src",actualVisit.place.photo_url);
        $("#photo_url").show();
    }
    else {
        // tyring looking for a picture in Flickr
        // var picture_query = actualVisit.place.display_name.split(',')[0] + " " + window.sessionStorage.getItem("city");
        // find_pic(picture_query);
    }
    $("#info").html(actualVisit.place.display_name.replace(",","<br/>"));
    if(actualVisit.place.description != null) {
        $("#description").html(actualVisit.place.description);
        $("#description").show()
    }
    if(actualVisit.place.www != null) {
        var href = actualVisit.place.www.trim();
        console.log("www ==> " +href);
        if(href.startsWith("\""))
            href = href.split(",")[0].substring(1);
        console.log("www ==> " +href);

        $("#www").attr("href",href);
        $("#www").show()
    }


    $("#www").click(function() {
        window.location = $(this).attr("href")
    });


    $("#share_btn").click(function() {
        var place_name = $("#title").html();
        //alert(place_name)
        openFB.api({
            method: 'POST',
            path: '/me/feed',
            params: {
                message: "Ho scoperto "+place_name+" grazie a Lume Planner!",
                link: "https://play.google.com/store/apps/details?id=it.unimore.morselli.lume"
            },
            success: function() {
                alert('Informazione condivisa su Facebook');
            },
            error: function() {
                alert('Si è verificato un problema con la condivisione');
            }
        });
    });

    $("#close").click(function() {
        window.history.back()
    });

    $("#next").click(function() {
        $(this).css("opacity","0.5");
        //if(!currentVisit) {
            var user = JSON.parse(window.localStorage.getItem("user"));
            var city = window.sessionStorage.getItem("city");
            var d = new Date().getTime();
            var request = {
                user: user.email,
                visited: currentDestination.place,
                time: d,
                rating: 10, // ???
                city: city
            };
            console.log(request);

            $.postJSON(conf.dita_server + "visited", request, function (data, status) {

                //console.log(JSON.stringify(data))
                window.sessionStorage.setItem("visitplan", JSON.stringify(data));
                window.location.href = "next_step.html"
            });
       //}
    });
});