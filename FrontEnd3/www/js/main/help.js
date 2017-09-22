$(document).ready(function(){

    var userid = JSON.parse(window.localStorage.getItem("user")).email.substring(2);

    $("#userid").html(userid);

    $("#help").click(function() {
        var email = "marco.mamei@gmail.com";
        var subject = "Lume help request";
        var emailBody = $("#text").val()+"%0D%0A from: "+ userid;
        window.location = 'mailto:' + email + '?subject=' + subject + '&body=' +   emailBody;
    });

    $("#home").click(function() {
        window.location = "index.html"
    });

    setTimeout(function () {
        //$('.foo').addClass('bar');
        $("#text").css({
            'height': 'auto'
        });
    }, 100);

});