$(document).ready(function(){


    $("#help").click(function() {
        var email = "marco.mamei@gmail.com";
        var subject = "Lume help request";
        var emailBody = $("#text").val()+"%0D%0A from: "+ JSON.parse(window.localStorage.getItem("user")).email;
        window.location = 'mailto:' + email + '?subject=' + subject + '&body=' +   emailBody;
    });

    $("#home").click(function() {
        window.location = "index.html"
    })

});