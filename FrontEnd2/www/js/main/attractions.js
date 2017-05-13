$(document).ready(function(){




    var sel = window.sessionStorage.getItem("sel");
    console.log(sel);
    var pois  = JSON.parse(window.sessionStorage.getItem("pois"));
    console.log(pois);
    //console.log(pois.attractions.length)
    var spois  = JSON.parse(window.sessionStorage.getItem("spois"));
    if(!spois) spois = [];

    pois[sel].forEach(function(item) {
        var name = (item.display_name.split(',')[0].length>25) ? item.display_name.split(',')[0].substring(0,25).trim()+"." : item.display_name.split(',')[0];
        var selected = false;
        spois.forEach(function(sitem) {
            //var sname = (sitem.display_name.split(',')[0].length>25) ? sitem.display_name.split(',')[0].substring(0,25).trim()+"." : sitem.display_name.split(',')[0];
            if(sitem.place_id === item.place_id)
                selected = true;
        });
        $("#arrayCreator").append(formatBlock(item.place_id,name,selected))
    });


    function contains(poi, spois){
        for(var i=0; i<spois.length;i++)
            if(poi.place_id == spois[i].place_id)
                return true;
        return false;
    }



    $("#done").click(function(e) {

        $("input:checked").each(function() {
            var x = $(this).context.defaultValue;
            //console.log(x)
            pois[sel].forEach(function(item) {
                //var name = (item.display_name.split(',')[0].length > 25) ? item.display_name.split(',')[0].substring(0, 25).trim() + "." : item.display_name.split(',')[0];
                if(item.place_id === x && !contains(item,spois)) {
                    console.log(item);
                    spois.push(item)
                }
            });
        });

        window.sessionStorage.setItem("spois",JSON.stringify(spois));
        window.location.href = "itinerary_create.html";
    });

});/**
 * Created by marco on 03/05/2017.
 */
