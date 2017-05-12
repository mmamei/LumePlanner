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
            //var sname = (sitem.display_name.split(',')[0].length>25) ? sitem.display_name.split(',')[0].substring(0,25).trim()+"." : sitem.display_name.split(',')[0];
            if(sitem.place_id === item.place_id)
                selected = true;
        });
        $("#arrayCreator").append(formatBlock(item.place_id,name,selected))
    });





    $("#done").click(function(e) {
        spois[sel] = [];
        $("input:checked").each(function() {
            var x = $(this).context.defaultValue;
            //console.log(x)
            pois[sel].forEach(function(item) {
                //var name = (item.display_name.split(',')[0].length > 25) ? item.display_name.split(',')[0].substring(0, 25).trim() + "." : item.display_name.split(',')[0];
                if(item.place_id === x) {
                    spois[sel].push(item)
                }
            });
        });

        console.log(spois[sel]);

        window.sessionStorage.setItem("spois",JSON.stringify(spois));
        window.location.href = "itinerary_create.html";
    });

});/**
 * Created by marco on 03/05/2017.
 */
