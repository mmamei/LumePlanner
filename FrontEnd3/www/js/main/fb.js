
/*
 function myTok() {
 alert(localStorage.getItem("fbAccessToken"))
 }
 */
//openFB.init({appId: '144640029403557'});
// Defaults to sessionStorage for storing the Facebook token

//  Uncomment the line below to store the Facebook token in localStorage instead of sessionStorage
openFB.init({appId: '144640029403557', tokenStore: window.localStorage});

function login() {
    openFB.login(
        function(response) {
            if(response.status === 'connected') {
                //alert('Facebook login succeeded, got access token: ' + response.authResponse.accessToken);
                location.reload();
            } else {
                alert('Facebook login failed: ' + response.error);
            }
        }, {scope: 'email,user_birthday,user_likes,publish_actions'});
}

function getInfo() {
    openFB.api({
        path: '/me',
        params: {
            fields: "birthday,gender,first_name,last_name,id,likes.limit(200){about,category,name}"
        },
        success: function(data) {
            console.log("ok ");
            all.id = window.localStorage.getItem("user");
            all.generalInfo = data;
            all.eventsInfo = [];


            //document.getElementById("userLikes").innerHTML = "<ul>";
            idLikes = [];
            idPlace = [];
            for(var i=0; i<data.likes["data"].length; i++){
                //document.getElementById("userLikes").innerHTML += "<li>"+ (i+1) + ") " + data.likes["data"][i]["name"] + " (" + data.likes["data"][i]["category"] + ") info: " + data.likes["data"][i]["about"];
                idLikes.push(data.likes["data"][i]["id"])
            }
            for(var i=0; i<idLikes.length; i++)
                findEvent(idLikes[i],all);


            $("#userName").text(data.first_name);
            //$("#userName").text(data.first_name+" "+data.last_name);
            $("#userPic").attr("src","http://graph.facebook.com/" + data.id + "/picture?type=normal");
        },
        error: errorHandler});
}






function findEvent(id,all) {
    openFB.api({
        path: "/"+ id + "/events",
        params: {
            fields: "name,place,start_time,end_time",
            limit: 100000
        },
        success: function(data) {
            //var doc = document.getElementById("events");
            //doc.innerHTML += "<ul>";
            //console.log(data)
            if(data["data"]["length"] != 0)
                for (var i=0 ; i<data["data"]["length"]; i++) {
                    var actualDate = new Date(formatLocalDate());
                    //console.log(actualDate)
                    var eventDate = new Date(data["data"][i]["start_time"]);
                    //console.log(eventDate)
                    if (eventDate-actualDate >=0) {
                        all.eventsInfo.push(data["data"][i]);
                        //doc.innerHTML += "<li> " + data["data"][i]["name"] + " alle ore " + eventDate;
                        if (data["data"][i]["place"]){
                            //doc.innerHTML += "presso -> " + data["data"][i]["place"]["name"];
                            var place_id = data["data"][i]["place"]["id"];
                            //doc.innerHTML += "<span id='" + place_id + "'></span>";
                            if(place_id){
                                findPlaceLoc(place_id)
                            }
                        }
                    }
                }
            //doc.innerHTML += "</ul>"
        },
        error: errorHandler});

}

function findPlaceLoc(id) {
    openFB.api({
        path: "/"+ id,
        params: {
            fields: "location",
        },
        success: function(data) {
            //console.log(data)
            //var doc = document.getElementById(data["id"]);
            //doc.innerHTML += " <b>lat</b> ->" + data["location"]["latitude"] + ", <b>long</b> -> " + data["location"]["longitude"]
        },
        error: errorHandler});

}


function extendTok() {
    var personalTok = localStorage.getItem("fbAccessToken");
    openFB.api({
        path: '/oauth/access_token',
        params: {
            grant_type: "fb_exchange_token",
            client_id: "144640029403557",
            client_secret: "edeb4fc2a84b0c903d1c8cdfc3de3f24",
            fb_exchange_token: personalTok
        },
        success: function(data) {
            //alert("estensione con successo")
            localStorage.setItem("fbAccessToken",data["access_token"])
        },
        error: errorHandler});
}


function formatLocalDate() {
    var now = new Date(),
        tzo = -now.getTimezoneOffset(),
        dif = tzo >= 0 ? '+' : '-',
        pad = function(num) {
            var norm = Math.abs(Math.floor(num));
            return (norm < 10 ? '0' : '') + norm;
        };
    return now.getFullYear()
        + '-' + pad(now.getMonth()+1)
        + '-' + pad(now.getDate())
        + 'T' + pad(now.getHours())
        + ':' + pad(now.getMinutes())
        + ':' + pad(now.getSeconds())
        + dif + pad(tzo / 60)
        + ':' + pad(tzo % 60);
}



/*
 function readPermissions() {
 openFB.api({
 method: 'GET',
 path: '/me/permissions',
 success: function(result) {
 alert(JSON.stringify(result.data));
 },
 error: errorHandler
 });
 }

 function revoke() {
 openFB.revokePermissions(
 function() {
 alert('Permissions revoked');
 },
 errorHandler);
 }
 */
function logout() {
    openFB.logout(
        function() {
            //alert('Logout avvenuto con successo');
            location.reload();
        },
        errorHandler);
}

function errorHandler(error) {

    // alert(error.message); //Invalid OAuth access token.
    localStorage.setItem("fbAccessToken","");
    location.reload()
}


var all = {};


$(document).ready(function(){

    if(localStorage.getItem("fbAccessToken")!=null){
        getInfo();
        extendTok();
        $("#logged").show();
        $("#not_logged").hide();


        window.setTimeout(function(){
            //console.log("---------------------------")
            //console.log(all)
            $.postJSON(conf.dita_server+"fb",all,function(data, status){
                //alert(data)
                //alert(status)
                //$("#home_btn").css("display","block")
            });
        },5000)


    }
    else{
        $("#logged").hide();
        $("#not_logged").show()
    }


        $("#home_btn").click(function(){
            window.history.back();
        });

    $("#login_btn").click(function () {
        login()
    });
    $("#logout_btn").click(function () {
        logout()
    })
});
