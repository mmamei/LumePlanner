$(document).ready(function(){




    var sel = window.sessionStorage.getItem("sel");
    console.log(sel);
    var pois  = JSON.parse(window.sessionStorage.getItem("pois"));
    //console.log(pois.attractions.length)
    var spois  = JSON.parse(window.sessionStorage.getItem("spois"));


    pois[sel].forEach(function(item) {
        var name = (item.display_name.split(',')[0].length>25) ? item.display_name.split(',')[0].substring(0,25).trim()+"." : item.display_name.split(',')[0];

        var selected = false;
        spois[sel].forEach(function(sitem) {
            var sname = (sitem.display_name.split(',')[0].length>25) ? sitem.display_name.split(',')[0].substring(0,25).trim()+"." : sitem.display_name.split(',')[0];
            if(sname === name)
                selected = true;
        });


        $("#select").append(" <option "+(selected ? "selected='selected'" : "")+">"+name+"</option>")
    });



    $("option").mousedown(function(e) {
        e.preventDefault();
        $(this).attr('selected', $(this).attr('selected') ? false : true);
        return false;
    });

    $("#done").click(function(e) {

        spois[sel] = [];
        $("option:selected").each(function() {

            var x = $(this).text();

            pois[sel].forEach(function(item) {
                var name = (item.display_name.split(',')[0].length > 25) ? item.display_name.split(',')[0].substring(0, 25).trim() + "." : item.display_name.split(',')[0];
                if(name == x) {
                    spois[sel].push(item)
                }
            });
        });


        window.sessionStorage.setItem("spois",JSON.stringify(spois));
        window.location.href = "itinerary_create.html";
    });

});/**
 * Created by marco on 03/05/2017.
 */
